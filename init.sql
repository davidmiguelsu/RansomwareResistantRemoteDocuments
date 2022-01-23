DROP TABLE IF EXISTS user_files;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS files;



CREATE TABLE users (
	u_id	 SERIAL,
	username VARCHAR(512),
	passwd	 VARCHAR(512),
	UNIQUE(username),
	PRIMARY KEY(u_id)
);

CREATE TABLE files (
	f_id	 SERIAL,
	filename VARCHAR(512),
	filehash VARCHAR(512),
	UNIQUE(filename),
	PRIMARY KEY(f_id)
);

CREATE TABLE user_files (
	id	 SERIAL,
	user_id	 BIGINT,
	file_id	 BIGINT,
	read_perm	 BOOL,
	write_perm   BOOL,
	file_owner   BOOL,
	PRIMARY KEY(id),
	FOREIGN KEY (user_id) REFERENCES users (u_id),
	FOREIGN KEY (file_id) REFERENCES files (f_id)
);
