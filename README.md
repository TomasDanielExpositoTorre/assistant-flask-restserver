# Assistant Flask Restserver

A lightweight Flask server wrapper for making REST requests within a Home Assistant setup.

---

## Table of Contents
- [API Setup](#api-setup)
- [HTTPS Hosting](#https-hosting)
    - [Static IP Hosting](#static-ip-hosting)
    - [mDNS Setup](#mdns-setup)
    - [Port Forwarding Setup](#port-forwarding-setup)
    - [LetsEncrypt DNS Challenge](#letsencrypt-dns-challenge)
- [WearOS Integration](#integration-with-wear-os-wear-smart)
    - [Static IP Hosting](#static-ip)
    - [URL Hosting](#port-forwarding-or-dns-challenge)
    - [Running the App](#running-the-app-android-studio)
- [Extending the App](#extending-the-app)
    - [Adding a Service](#adding-a-service)
    - [Device Support](#device-support)
    - [Extending the WearOS App](#extending-the-wearos-app)
---


## API Setup

To set up an environment for the main API, install the required modules in requirements.txt:

```bash
apt install python3-virtualenv
virtualenv venv
source venv/bin/activate
pip3 install -r requirements.txt
```

Then create a copy of the .env.example file and replace with your own environment variables:
```bash
BASE_URL="your-homeassistant-url-here"
API_URL="your-local-url-here" # If you host the API with an URL
TOKEN="your-homeassistant-authtoken-here"
```

Then, after running one of the [HTTPS Hosting](#https-hosting) options down below, you can start the service:

```bash
cd flask-rest
python3 api.py
```
---

## HTTPS Hosting

### Static IP hosting

You can host the base API as a service with a defined, static IP. If you want to include HTTPS encrypted communication for the service, modify the [server.conf](flask-rest/data/server.conf) file to point to your IP, then generate a self-signed certificate:

```bash
cd flask-rest/data
source certgen.sh
```

For any application to trust this certificate, the `server.pem` file that this service outputs has to be provided to it. 

### mDNS Setup

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

The avahi-daemon service allows to resolve your address as {hostname}.local, where `hostname` can be changed in the following configuration file:

```conf
# In /etc/avahi/avahi-daemon.conf
[server]
host-name=glados # Or your preferred hostname
allow-interfaces=eth0,wlan0,wlo1 # Change with the interface your service is running in
```

After this, create a service file for the API:

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

To work over HTTPS, you will need to generate a self-signed certificate. You can use the provided script for this after changing the DNS field in [server.conf](flask-rest/data/server.conf):

```bash
cd flask-rest/data
source certgen.sh
```

For any application to trust this certificate, the `server.pem` file that this service outputs has to be provided to it.

### Port Forwarding setup

Instead of using mDNS, you can acquire a DDNS domain name through some of the following services:

- [DuckDNS](https://www.duckdns.org/)
- [DynuDNS](https://www.dynu.com/en-US)

After going to your router's webpage (generally 192.168.x.1) and forwarding port 443 to your service, you can install certbot to generate a LetsEncrypt certificate:

```bash
sudo apt install certbot
certbot certonly --standalone -d YOURDOMAINNAMEHERE
```

After generating the certificate, place the `server.crt` and `server.key` files in the data directory.

### LetsEncrypt DNS Challenge

If you don't want to expose your service ports to the internet, you can instead generate a certificate through a LetsEcrypt DNS-01 challenge.

- For an acquired domain name, you can follow along this [example](https://ongkhaiwei.medium.com/generate-lets-encrypt-certificate-with-dns-challenge-and-namecheap-e5999a040708).

- For DynuDNS, you can follow along the example in the plugin [documentation](https://pypi.org/project/certbot-dns-dynu/).

- For DuckDNS, you can follow along the example in the plugin [documentation](https://pypi.org/project/certbot-dns-duckdns/)

This option is also compatible with self-signed certificates, where you can:
1. Generate the LetsEncrypt certificate through a reverse-proxy app (for example, [Nginx Proxy Manager](https://nginxproxymanager.com/)).
2. Launch the API with a self-signed certificate like [here](#static-ip-hosting).
3. Redirect all petitions from the proxy manager to the API IP address.

---

## Integration with Wear-OS (wear-smart)

WearOS enforces HTTPS communication, but address resolution through mDNS is unstable and not supported by default.

### Static IP

If you chose to setup this service through a static IP, simply running the certificate generation script will create a copy (server_cert.crt) for the WearOS app. You must recompile the wear-smart application after generating this script and reflect your new hostname in the following files:

- NetworkCommunicator.kt
- network_security_config.xml

### Port Forwarding or DNS Challenge

If you chose to generate a LetsEncrypt ceritifcate by either of the methods mentioned above, update the API endpoint in `NetworkCommunicator.kt` with your new URL.

### Running the App (Android Studio)

To compile the app in android studio (linux), click on `Build > Generate App Bundle or APKs > Generate APKs`, which can then be installed through `adb` in your watch:
```bash
adb pair watch-ip:watch-port
adb connect watch-ip:watch-port
cd wear-smart/app/build/outputs/apk/debug
adb -s watch-ip:watch-port install app.debug.apk
```

To enable adb debugging in a wearOS smartwatch, follow this [guide](https://developer.android.com/training/wearables/get-started/debugging)

---

## Extending the App

### Adding a Service

Any service that consumes the API can do so from either the defined IP or URL address, as you've set up above. An example service is set up for voice recognition with BetterWhisper and ffmpeg (the [listener](flask-rest/services/listener.py) and its corresponding [ruleset](flask-rest/services/rules.py)) but the required packages have not been included in `requirements.txt` to keep the solution more lightweight.

### Device Support
This version of the API only provides support for smart lights. To add compatibility with another kind of smart device (for example, smart curtains), you have to:
1. Duplicate `light.py` and rename the file/class it to whichever device you want to support (e.g., `curtain.py`).
2. Change the constructor and `data()` method to obtain a representation based on the device's attributes.
3. Change the following line in `api.py`:
```python
# Old
supported_devices = {"light": Light}

# New
supported_devices = {"light": Light, "curtain": Curtain}
```

### Extending the WearOS App
After extending the API with new devices, you can also do the same for your smartwatch WearOS app through the following steps:
1. Create the root folder of your new device type [here](wear-smart/app/src/main/java/com/example/wearsmart/presentation/).
2. Create your implementation for the new device type at the root folder.
3. Add a call to your device implementation (e.g., "curtain") [here](wear-smart/app/src/main/java/com/example/wearsmart/presentation/DeviceList.kt#L56).
4. Rebuild the app and send it to your watch like [explained above](#running-the-app-android-studio).