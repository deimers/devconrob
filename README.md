Source code used in my Luminis DevCon 2016 [presentation](https://devcon.luminis.eu/creating-a-cheap-as-chips-robot-with-a-java-brain/) about creating a cheap robot using Java on the Raspberry Pi.  

To prepare a Raspberry Pi with JDK8 for ARM processors with OpenJDK Device I/O (DIO) compiled from source follow these instructions:


### Installation Guide

This guide assumes you already [installed an image](https://www.raspberrypi.org/documentation/installation/installing-images/) of the latest [raspbian OS](https://www.raspberrypi.org/downloads/raspbian/) and just booted and logged into your Pi.

First upgrade to the latest and greatest:

    sudo apt-get update && sudo apt-get upgrade

Install JDK8 for ARM:

    sudo apt-get install oracle-java8-jdk
    
Install [OpenJDK DIO](http://hg.openjdk.java.net/dio/dev) from source:

    sudo apt-get install mercurial
    cd ~
    hg clone http://hg.openjdk.java.net/dio/dev
    cd dev
    export PI_TOOLS=/usr
    export JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/
    make

NOTE: The exact path of your `JAVA_HOME` may vary depending on the revision of your Pi.

Install [servoblaster](https://github.com/richardghirst/PiBits.git) for controlling a Servo with PWM:

    sudo apt-get install git
    cd ~
    git clone https://github.com/richardghirst/PiBits.git
    cd PiBits/ServoBlaster/user/
    make servod

Start the driver using:

    sudo ~/PiBits/ServoBlaster/user/servod --p1pins=7

You should see something like this: 

    Board revision:                  2
    Using hardware:                PWM
    Using DMA channel:              14
    Idle timeout:             Disabled
    Number of servos:                1
    Servo cycle time:            20000us
    Pulse increment step size:      10us
    Minimum width value:            50 (500us)
    Maximum width value:           250 (2500us)
    Output levels:              Normal

    Using P1 pins:                   7

    Servo mapping:
     0 on P1-7                  GPIO-4

NOTE: ServoBlaster refers to the pins using the 'board numbering scheme' (where pins are just counted from the top-left (1) via top-left (2) and so on). The 7th pin is referred to as pin 'GPIO-4' using the 'Broadcom SOC channel numbering scheme'.  

This allows us to control a single PWM device using 'GPIO-4'.

### Enable WiFi
Edit the following file:

    /etc/wpa_supplicant/wpa_supplicant.conf

Create a 'network' block to fit your wireless configuration. In my case this worked just fine: 

    network={
       ssid="nameofmynetwork"
       psk="mysecretpassword"
       proto=WPA RSN
       key_mgmt=WPA-PSK
       pairwise=CCMP TKIP
       group=CCMP TKIP
    }
This should be picked up automatically, but if you're unlucky: `sudo reboot`.

#### Your connection freezes when idle?
This may turn out to be a pretty well known stability issue of Realtek 8192CU Based Wifi Modules. It is recommended to disable power management by adding a the following file:

    /etc/modprobe.d/8192cu.conf

And add this line:

    options 8192cu rtw_power_mgnt=0 rtw_enusbss=0

