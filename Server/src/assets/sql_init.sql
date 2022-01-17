
CREATE DATABASE ransom;
DROP TABLE users CASCADE;
DROP TABLE files CASCADE;
DROP TABLE user_files CASCADE;


CREATE TABLE users (
	u_id		 BIGINT,
	username	 VARCHAR,
	password	 VARCHAR,
	PRIMARY KEY(u_id)
);


CREATE TABLE files (
	f_id		 BIGINT,
	file_name	 VARCHAR,
	PRIMARY KEY(f_id)
);

CREATE TABLE user_files (
	id	 BIGINT,
	user_id	 BIGINT,
	file_id	 BIGINT,
	read_perm	 BOOL,
	write_perm BOOL,
	PRIMARY KEY(id),
  	FOREIGN KEY(user_id) REFERENCES users (u_id),
	FOREIGN KEY(file_id) REFERENCES files (f_id)
);
-- ALTER TABLE users ADD CONSTRAINT users_fk1 FOREIGN KEY (user_files_id) REFERENCES user_files(id);
-- ALTER TABLE files ADD CONSTRAINT files_fk1 FOREIGN KEY (user_files_id) REFERENCES user_files(id);


