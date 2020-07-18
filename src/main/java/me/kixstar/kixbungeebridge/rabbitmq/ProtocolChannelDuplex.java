package me.kixstar.kixbungeebridge.rabbitmq;

//todo: implement
public abstract class ProtocolChannelDuplex extends ProtocolChannelInput{



    private String channel;

    private ProtocolChannelDuplex(String channel) {
        this.channel = channel;

    }

    public void send(Packet packet, String route) {

        //server.sendData(this.getChannel(), frame.toByteArray());

    }

    public String getChannel() {
        return channel;
    }

}
