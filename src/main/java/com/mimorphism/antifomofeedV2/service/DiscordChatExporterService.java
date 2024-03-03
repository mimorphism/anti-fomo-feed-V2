package com.mimorphism.antifomofeedV2.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimorphism.antifomofeedV2.dto.discordlog.DiscordLog;
import com.mimorphism.antifomofeedV2.dto.discordlog.Message;
import com.mimorphism.antifomofeedV2.enums.FailedExportErrorType;
import com.mimorphism.antifomofeedV2.exceptions.AccessDeniedException;
import com.mimorphism.antifomofeedV2.exceptions.ChannelDoesntExistException;
import com.mimorphism.antifomofeedV2.exceptions.NothingToExportException;
import com.mimorphism.antifomofeedV2.repository.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DiscordChatExporterService {


    private static final String DCE_EXCEPTION_ACCESS_IS_FORBIDDEN = "DiscordChatExporter.Core.Exceptions.DiscordChatExporterException: Access is forbidden.";
    private static final String DCE_EXCEPTION_EXPORT_FAILED = "Export failed.";
    private static final Logger log = LoggerFactory.getLogger(DiscordChatExporterService.class);
    private static final String DCE_EXCEPTION_CHANNEL_DOESNT_EXIST = "DiscordChatExporter.Core.Exceptions.DiscordChatExporterException: Requested resource (channels/%s) does not exist.";
    private final String DISCORD_TOKEN;
    private final String DISCORD_COMMAND_PATH;
    private final DiscordServerRepo discordServerRepo;
    private final ChannelRepo channelRepo;
    private final FeedItemRepo feedItemRepo;

    private final StatsService statsService;
    private final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yy hh:mm a");
    private final String FILENAME_FORMAT = "discordlogs/%G-%C-lastExportedDate.json";

    private int NO_OF_DISCORDLOGFILES_TO_PROCESS_PER_BATCH = 0;

    public DiscordChatExporterService(
            FeedItemRepo feedItemRepo,
            @Value("${discord.token}") String discordToken,
            @Value("${discord.chat.exporter.command.path}") String discordCommandPath,
            StatsRepo statsRepo,
            DiscordServerRepo discordServerRepo,
            ChannelRepo channelRepo,
            StatsService statsService, @Value("${no.of.files.to.process.for.discordlogfile.per.batch}") int noOfDiscordLogFilesToProcessPerBatch) {
        this.discordServerRepo = discordServerRepo;
        this.DISCORD_TOKEN = discordToken;
        this.DISCORD_COMMAND_PATH = Paths.get("")
                .toAbsolutePath()
                .toString() + discordCommandPath;
        this.channelRepo = channelRepo;
        this.feedItemRepo = feedItemRepo;
        this.statsService = statsService;
        this.NO_OF_DISCORDLOGFILES_TO_PROCESS_PER_BATCH = noOfDiscordLogFilesToProcessPerBatch;
    }

    private String getCustomFilenameWithLastExportedDate(String lastExportedDate) {
        var newFilename = FILENAME_FORMAT.replaceAll("lastExportedDate", lastExportedDate);
        return newFilename;
    }


    @Async
    @Scheduled(cron = "${processing.schedule.discord.export}")
    public void triggerDiscordProcess() {
        getDiscordServers();
        getChannelsFromServers();
        log.info("Staring discord chat export process");
        var servers = discordServerRepo.findAll();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        try {
            if (!servers.isEmpty()) {
                for (DiscordServer server : servers) {
                    log.info("Processing server {}", server.getName());
                    var channels = server.getChannels();
                    if (!channels.isEmpty()) {
                        for (String channelId : channels) {
                            log.info("Processing channel {}", channelId);
                            executor.submit(() -> {
                                var channel = channelRepo.findById(Long.valueOf(channelId)).get();
                                ProcessBuilder process;
                                if (channel.getLastExported() == null) {
                                    process = exportSingleChannelPB(channelId);
                                } else {
                                    process = exportSingleChannelPBWithLastExportedDateTime(channelId, channel.getLastExported().format(DATETIME_FORMAT));
                                }
                                startDiscordExporterProcess(process, channel);
                            });
//                                    , 0, 30, TimeUnit.SECONDS);
                        }
                    }

                }
                executor.shutdown();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////
    ///             IMPLEMENTATION DETAIL               ///
    ///////////////////////////////////////////////////////

    private void getDiscordServers() {
        try {
            ProcessBuilder builder =
                    getServerListPB();
            builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process p = builder.start();
            try (BufferedReader buf =
                         new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                AtomicInteger lineCount = new AtomicInteger(0);
                buf.lines().forEach(line -> {
                    lineCount.incrementAndGet();
                    log.info(line);
                    if (lineCount.get() != 1) {
                        var serverDetails = line.split("\\|");
                        var server = new DiscordServer();
                        server.setId(Long.parseLong(serverDetails[0].replaceAll("\\s", "")));
                        server.setName(serverDetails[1]);
                        discordServerRepo.save(server);
                    }
                });
            }
            p.waitFor();
        } catch (InterruptedException | IOException ex) {
            log.error(ex.getMessage());
        }
    }

    private void getChannel(DiscordServer server) {
        try {
            List<String> channels = new ArrayList<>();
            ProcessBuilder builder =
                    getChannelsFromServerPB(String.valueOf(server.getId()));
            builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process p = builder.start();
            try (BufferedReader buf =
                         new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                AtomicInteger lineCount = new AtomicInteger(0);
                buf.lines().forEach(line -> {
                    lineCount.incrementAndGet();
                    log.info(line);
                    var channelDetails = line.split("\\|");
                    channels.add(channelDetails[0].replaceAll("\\s", ""));
                    var channel = channelRepo.findById(Long.valueOf(channelDetails[0].replaceAll("\\s", "")));
                    if (channel != null && channel.isPresent()
//                            && StringUtils.isBlank(channel.get().getName()) && StringUtils.isBlank(channel.get().getServerName())) {
                    ) {
                        channel.get().setName(Arrays.stream(channelDetails).skip(1).collect(Collectors.joining()));
                        channel.get().setServerName(server.getName());
                        channelRepo.save(channel.get());
                    } else if (channel.isEmpty()) {
                        var newChannel = new Channel();
                        newChannel.setId(Long.valueOf(channelDetails[0].replaceAll("\\s", "")));
                        newChannel.setName(Arrays.stream(channelDetails).skip(1).collect(Collectors.joining()));
                        newChannel.setServerName(server.getName());
                        channelRepo.save(newChannel);
                    }
                });
            }
            p.waitFor();
            server.setChannels(channels);
            discordServerRepo.save(server);
            log.info("Saved server with channels: {}", server);
        } catch (Exception ex) {
            log.error(ex.getMessage());

        }

    }

    private void getChannelsFromServers() {
        var servers = discordServerRepo.findAll();
        if (!servers.isEmpty()) {
            for (DiscordServer server : servers) {
                getChannel(server);
            }
        }

    }


    @Scheduled(cron = "${processing.schedule.discord.processdata}")
    public void processDiscordData() {
        File directory = new File("discordlogs");
        File[] contents = directory.listFiles();
        Arrays.asList(contents).stream().limit(NO_OF_DISCORDLOGFILES_TO_PROCESS_PER_BATCH).forEach(
                f -> {
                    if (FilenameUtils.getExtension(f.getName()).equals("json")) {
                        try {
                            log.info("Processing {}", f.getName());
                            var discordLog = new ObjectMapper().readValue(Paths.get(f.getPath()).toFile(), DiscordLog.class);
                            log.info("Processing {} messages for this file", discordLog.getMessageCount());
                            for (Message link : discordLog.getMessages()) {
                                if (!link.getEmbeds().isEmpty()) {
                                    log.info("Processing url {}", link.getEmbeds().get(0).getUrl());
                                    var feed = new FeedItem();
                                    if (!StringUtils.isBlank(link.getEmbeds().get(0).getDescription())) {
                                        feed.setDescription(link.getEmbeds().get(0).getDescription());
                                    }
                                    if (link.getEmbeds().get(0).getThumbnail() != null && !StringUtils.isBlank(link.getEmbeds().get(0).getThumbnail().getUrl())) {
                                        feed.setImage(link.getEmbeds().get(0).getThumbnail().getUrl());
                                    }
                                    feed.setDomain(getDomainName(link.getEmbeds().get(0).getUrl()));
                                    if (!StringUtils.isBlank(link.getEmbeds().get(0).getTitle())) {
                                        feed.setTitle(link.getEmbeds().get(0).getTitle());
                                    }
                                    feed.setUrl(link.getEmbeds().get(0).getUrl());
                                    feed.setSource("DISCORD");
                                    feed.setCreationDate(LocalDateTime.now());
                                    feedItemRepo.save(feed);
                                    log.info("Succesfully saved feed item for url {}", link.getEmbeds().get(0).getUrl());
                                    statsService.sendStatsUpdate();
                                }
                            }
                        } catch (IOException e) {
                            log.error("Error in processing discord logs: {}", e);
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        Files.deleteIfExists(f.getAbsoluteFile().toPath());
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete exported log file " + f.getName());
                    }
                }
        );

    }

    private String getDomainName(String url) {
        try {
            var uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException ex) {
            return "NO DOMAIN";
        }
    }

    private void startDiscordExporterProcess(ProcessBuilder process, Channel channel) {
        try {
            ProcessBuilder builder =
                    process;
            Process p = builder.start();
            InputStream processStdOutput = p.getErrorStream();
            Reader r = new InputStreamReader(processStdOutput);
            BufferedReader br = new BufferedReader(r);
            String line;
            FailedExportErrorType error = null;
            var unknownErrorPresent = false;
            while ((line = br.readLine()) != null) {
                if (StringUtils.contains(line, DCE_EXCEPTION_ACCESS_IS_FORBIDDEN)) {
                    error = FailedExportErrorType.ACCESS_IS_FORBIDDEN;
                } else if (StringUtils.contains(line, String.format(DCE_EXCEPTION_CHANNEL_DOESNT_EXIST, channel.getId()))) {
                    error = FailedExportErrorType.CHANNEL_DOESNT_EXIST;
                } else if (StringUtils.contains(line, DCE_EXCEPTION_EXPORT_FAILED) ||
                        StringUtils.containsIgnoreCase(line, "error")) {
                    unknownErrorPresent = true;
                }

            }
            int processStatus = p.waitFor();
            if (processStatus != 0) {
                p.destroyForcibly();
            }
            if (error != null || unknownErrorPresent) {
                if (unknownErrorPresent && error == null) {
                    error = FailedExportErrorType.NOTHING_TO_EXPORT;
                }
                throwExceptionAccordingToErrorType(error, channel);
            } else {
                channel.setLastExported(LocalDateTime.now());
                channelRepo.save(channel);
                log.info("Succesfully saved channel {} of server: {}", channel.getName(), channel.getServerName());
            }
        } catch (ChannelDoesntExistException ex) {
            log.error("Export failed! : {}", ex.getMessage());
            log.info("Channel will be deleted  : {} server: {}", channel.getName(), channel.getServerName());
            //delete this channel because it doesn't exist anymore
            channelRepo.delete(channel);
            log.info("Channel succesfully deleted  : {}", channel.getName());
        } catch (NothingToExportException ex) {
            log.error("Export failed! Nothing to export!: {}", ex.getMessage());
        } catch (InterruptedException | IOException ex) {
            log.error("Error in discordchatexporter application!");
        }
    }

    private void throwExceptionAccordingToErrorType(FailedExportErrorType errorType, Channel channel) {
        if (errorType == FailedExportErrorType.ACCESS_IS_FORBIDDEN) {
            throw new AccessDeniedException(channel);
        } else if (errorType == FailedExportErrorType.CHANNEL_DOESNT_EXIST) {
            throw new ChannelDoesntExistException(channel);
        } else if (errorType == FailedExportErrorType.NOTHING_TO_EXPORT) {
            throw new NothingToExportException(channel);
        }
    }

    private ProcessBuilder exportSingleChannelPB(String channelId) {
        return new ProcessBuilder("dotnet", DISCORD_COMMAND_PATH, "export", "-c", channelId, "-t", DISCORD_TOKEN, "--filter", "has:link", "-f", "Json", "-p", "20mb", "-o", FILENAME_FORMAT)
                .redirectOutput(ProcessBuilder.Redirect.PIPE);
    }

    private ProcessBuilder exportSingleChannelPBWithLastExportedDateTime(String channelId, String lastExportedDateTime) {
        return new ProcessBuilder("dotnet", DISCORD_COMMAND_PATH, "export", "-c", channelId, "--after", lastExportedDateTime, "-t", DISCORD_TOKEN, "--filter", "has:link", "-f", "Json", "-p", "20mb", "-o", getCustomFilenameWithLastExportedDate(lastExportedDateTime))
                .redirectOutput(ProcessBuilder.Redirect.PIPE);
    }

    private ProcessBuilder getServerListPB() {
        return new ProcessBuilder("dotnet", DISCORD_COMMAND_PATH, "guilds", "-t", DISCORD_TOKEN);
    }

    private ProcessBuilder getChannelsFromServerPB(String serverId) {
        return new ProcessBuilder("dotnet", DISCORD_COMMAND_PATH, "channels", "-t", DISCORD_TOKEN, "-g", serverId);
    }


}
