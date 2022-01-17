INSERT INTO users (username, passwd) VALUES('user1', 'pw1');
INSERT INTO users (username, passwd) VALUES('user2', 'pw1');
INSERT INTO users (username, passwd) VALUES('user3', 'pw1');


INSERT INTO files (filename, filehash) VALUES('ficheiro2', '123123123123');
INSERT INTO files (filename, filehash) VALUES('ficheiro3', '123123123123');
INSERT INTO files (filename, filehash) VALUES('ficheiro4', '123123123123');

INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(8, 4, true, true);
INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(8, 5, true, true);
INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(9, 5, true, true);
INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(10, 4, true, true);


SELECT user_id FROM users WHERE username = 'user2';


SELECT read_perm FROM user_files WHERE 


register - user e added aos users.

write - adiciona aos files, adiciona aos user_files u_id + file_id

read - select read_perms do user_files

delete - if(select file_owner from user_files where file_id = .. && user_id = ... )
            deleteFile();

list - 

select fileName from files where f_id IN (select file_id from user_files where user_id = 'user2' && read_perm = true)
------
giveReadPerms(user_id, file_id) - INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(user_id, file_id, true, false);

giveAllPerms(user_id, file_id) - INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(user_id, file_id, true, true);

addOns:

get fileID, getuserID

