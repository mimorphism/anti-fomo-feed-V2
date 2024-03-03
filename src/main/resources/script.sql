-- public.feed_item definition

-- Drop table

-- DROP TABLE public.feed_item;

CREATE TABLE public.feed_item (
	item_id int8 NOT NULL,
	already_seen bool NOT NULL,
	description text NULL,
	image text NULL,
	marked_for_deletion bool NOT NULL,
	title text NULL,
	url text NOT NULL,
	CONSTRAINT feed_item_pkey PRIMARY KEY (item_id)
);
