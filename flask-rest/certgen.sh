openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout server.key -out server.crt -config server.conf
openssl x509 -in server.crt -out server.pem -outform PEM
cp server.pem ../wear-smart/app/src/main/res/raw/server_cert.crt
