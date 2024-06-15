ALTER TABLE video.file_info ADD COLUMN u_id BIGINT UNSIGNED DEFAULT 0 COMMENT '归属id';
CREATE INDEX INDEX_U_ID ON video.file_info (u_id);

ALTER TABLE video.file_info ADD COLUMN p_id BIGINT UNSIGNED DEFAULT 0 COMMENT '父级id';
CREATE INDEX INDEX_P_ID ON video.file_info (p_id);

ALTER TABLE video.file_info ADD COLUMN d_id BIGINT UNSIGNED DEFAULT 0 COMMENT '目录id';
CREATE INDEX INDEX_D_ID ON video.file_info (d_id);

ALTER TABLE video.`index` ADD COLUMN host_name varchar(100) COMMENT '主机名';
