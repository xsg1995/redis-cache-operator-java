package live.xsg.cacheoperator.codec;


import java.util.HashMap;
import java.util.Map;

/**
 * Codec实现工厂
 * Created by xsg on 2020/8/17.
 */
public class CodecFactory {

    private static Map<CodecEnum, Codec> codecMap = new HashMap<>();
    static {
        codecMap.put(CodecEnum.STRING, new StringCodec());
        codecMap.put(CodecEnum.MAP, new MapCodec());
    }

    private CodecFactory() {}

    public static Codec getByType(CodecEnum codecEnum) {
        if (codecEnum == null) {
            throw new IllegalArgumentException("codecEnum is empty.");
        }
        Codec codec = codecMap.get(codecEnum);
        if (codec == null) {
            throw new IllegalArgumentException("codec is empty.");
        }
        return codec;
    }
}
