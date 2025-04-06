# Assistant Flask Restserver
A simple flask REST server to communicate with a series of IoT devices over a local network.

## API Setup

To set up an environment for the main API, install the required modules in requirements.txt:
```bash
apt install python3-virtualenv
virtualenv venv
source venv/bin/activate
cd flask-rest
pip3 install -r requirements.txt
```

Then, after running additional setup steps below, you can start the service
```bash
python3 api.py
```


### mDNS Setup (Option A, default)

You can use mDNS (Multicast-DNS) to resolve the hostname of the API instead of using your IP address directly, if you don't plan on statically hosting this service. To install mDNS for a linux device, run the following commands:

```bash
# Debian/Ubuntu
apt update
apt install avahi-daemon avahi-utils

# CentOS
yum install avahi avahi-tools

# Fedora
dnf install avahi avahi-tools
```

The avahi-daemon service allows to resolve your address as {hostname}.local. The default, assumed hostname for this project is 'glados', but this can be changed in the configuration file:

```conf
# In /etc/avahi/avahi-daemon.conf
[server]
host-name=glados # Or your preferred hostname
allow-interfaces=eth0,wlan0,wlo1 # Change with the interface your service is running in
```

To work over HTTPS, you will need to generate a self-signed certificate. You can use the provided script for this:
```bash
cd flask-rest
source certgen.sh
python3 api.py
```

### Port Forwarding setup (Option B)
Instead of using mDNS, you can acquire a DDNS domain name through some of the following services:
- [DuckDNS](https://www.duckdns.org/)
- [DynuDNS](https://www.dynu.com/en-US) 

After going to your router's webpage (generally 192.168.x.1) and forwarding port 443 to your service, you can install certbot to generate a LetsEncrypt certificate:
```bash
sudo apt install certbot
certbot certonly --standalone -d YOURDOMAINNAMEHERE
```

### LetsEncrypt DNS Challenge (Option C)
If you don't want to expose your service ports to the internet, you can instead generate a certificate through a LetsEcrypt DNS-01 challenge. 

- For an acquired domain name, you can follow along this [example](https://ongkhaiwei.medium.com/generate-lets-encrypt-certificate-with-dns-challenge-and-namecheap-e5999a040708).

- For DynuDNS, you can follow along the example in the plugin [documentation](https://pypi.org/project/certbot-dns-dynu/).

- For DuckDNS, you can follow along the example in the plugin [documentation](https://pypi.org/project/certbot-dns-duckdns/)


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

## Integration with Wear-OS (wear-smart)

WearOS enforces HTTPS communication, so one of the three name resolution setups mentioned above must be followed for it to function correctly. 

### mDNS

If you chose option A in the setup above, simply running the certificate generation script will create a copy (server_cert.crt). You must recompile the wear-smart application after generating this script. If you want to use a domain name different from *glados.local*, please change it in the following files:

- server.conf (for the certificate generation)
- NetworkCommunicator.kt
- network_security_config.xml


### Port Forwarding or DNS Challenge

If you chose either option B or C in the setup above, update the API endpoint in `NetworkCommunicator.kt` to your new URL.

### Running the App (Android Studio)

To compile the app in android studio (linux), click on `Build > Generate App Bundle or APKs > Generate APKs`, which can then be installed through `adb` in your watch. To enable adb debugging in a wearOS smartwatch, follow this [guide](https://developer.android.com/training/wearables/get-started/debugging)