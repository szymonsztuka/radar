package radar;

import radar.message.DummyScriptFactory;
import radar.message.TxtDecoderFactory;
import radar.message.TxtEncoderFactory;

public class TxtBootstrap {

    public static void main(String[] args) {
        String[] configurationFiles = new String[]{"../../radar-samples/build/resources/main/sample.properties","../../radar-samples/build/resources/main/my-env.properties"};
        Radar me = new Radar(new TxtEncoderFactory(),
                new TxtDecoderFactory(),
                new DummyScriptFactory(),
                configurationFiles);
         me.run();
    }
}
