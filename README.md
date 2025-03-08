# Assistant Flask Restserver
A simple flask REST server to communicate with a series of IoT devices over a local network.

## Setup

### Environment

To set up the flask environment, install the required modules in requirements.txt:
```bash
apt install python3-virtualenv
virtualenv venv
source venv/bin/activate
cd flask-rest
pip3 install -r requirements.txt
```

### mDNS
To resolve petitions through the expected URL instead of an IP address, you will need to set up mDNS in your device.

```bash
# Debian/Ubuntu
apt update
apt install avahi-daemon avahi-utils

# CentOS
yum install avahi avahi-tools

# Fedora
dnf install avahi avahi-tools
```
Then change the hostname and interfaces in your configuration file:
```conf
# In /etc/avahi/avahi-daemon.conf
[server]
host-name=glados
allow-interfaces=eth0,wlan0,wlo1 # Change with the interface your service is running in
```
And create a service file for the API:
```xml
<!-- In /etc/avahi/services/assistant-flask.conf -->
<?xml version="1.0" standalone='no'?><!--*-nxml-*-->
<!DOCTYPE service-group SYSTEM "avahi-service.dtd">
<service-group>
    <name replace-wildcards="yes">Flask RESTserver</name>
    <service>
        <type>_http._tcp</type>
        <port>8000</port>
    </service>
</service-group>
```

### Encrypted Communication
To access your local API through HTTPS, you can generate a self-signed certificate using the provided script, then run the app:
```bash
source certgen.sh
python3 api.py
```

## Hostname
To use a custom hostname for the REST server, change the default value in the following files:
* flask-rest/server.conf
* /etc/avahi/avahi-daemon.conf

For integration with wearOS, additional files need to be changed:
* Home.kt, ObjectDetail.kt
* network_security_config.xml
Finally, regenerate the certificate and run the application
```bash
source certgen.sh
python3 api.py
```