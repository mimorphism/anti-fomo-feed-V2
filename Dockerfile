FROM eclipse-temurin:11.0.21_9-jre
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        curl \
        ca-certificates \
        \
        # .NET dependencies
        libc6 \
        libgcc1 \
        libgssapi-krb5-2 \
        libicu-dev \
        libssl3 \
        libstdc++6 \
        zlib1g \
    && rm -rf /var/lib/apt/lists/*

RUN curl -sSL https://dot.net/v1/dotnet-install.sh | bash /dev/stdin -Channel 7.0 -Runtime dotnet -InstallDir /usr/share/dotnet \
    && ln -s /usr/share/dotnet/dotnet /usr/bin/dotnet

RUN mkdir -p /home/app
WORKDIR /home/app
RUN mkdir -p /home/app/irclog

COPY antifomofeedV2-0.0.1-SNAPSHOT.jar ./
COPY discordchatexporter ./discordchatexporter
COPY url.log ./irclog
ENTRYPOINT ["java", "-jar", "antifomofeedV2-0.0.1-SNAPSHOT.jar"]
