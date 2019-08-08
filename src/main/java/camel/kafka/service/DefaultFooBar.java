package camel.kafka.service;

public class DefaultFooBar implements FooBar {

    @Override
    public String process(String incoming) {
        String returnValue = incoming;
        if (incoming != null) {
            returnValue = new StringBuilder(incoming).reverse().toString();
        }
        return returnValue;
    }
}
