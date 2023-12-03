public class Instance extends MessageAppInstance {
    public Instance(int port) {
        super(port, getIpAddress());
    }
}
