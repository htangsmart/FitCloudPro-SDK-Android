package com.github.kilnn.wristband2.sample.net;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by Kilnn on 2017/7/11.
 * fastjson ConverterFactory,Copy from fastjson_v1.2.34
 */
public class Retrofit2ConverterFactory extends Converter.Factory {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    private static final Feature[] EMPTY_SERIALIZER_FEATURES = new Feature[0];

    private ParserConfig parserConfig = ParserConfig.getGlobalInstance();
    private int featureValues = JSON.DEFAULT_PARSER_FEATURE;
    private Feature[] features;

    private SerializeConfig serializeConfig;
    private SerializerFeature[] serializerFeatures;

    public Retrofit2ConverterFactory() {
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, //
                                                            Annotation[] annotations, //
                                                            Retrofit retrofit) {
        return new ResponseBodyConverter<ResponseBody>(type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, //
                                                          Annotation[] parameterAnnotations, //
                                                          Annotation[] methodAnnotations, //
                                                          Retrofit retrofit) {
        return new RequestBodyConverter<RequestBody>();
    }

    public ParserConfig getParserConfig() {
        return parserConfig;
    }

    public Retrofit2ConverterFactory setParserConfig(ParserConfig config) {
        this.parserConfig = config;
        return this;
    }

    public int getParserFeatureValues() {
        return featureValues;
    }

    public Retrofit2ConverterFactory setParserFeatureValues(int featureValues) {
        this.featureValues = featureValues;
        return this;
    }

    public Feature[] getParserFeatures() {
        return features;
    }

    public Retrofit2ConverterFactory setParserFeatures(Feature[] features) {
        this.features = features;
        return this;
    }

    public SerializeConfig getSerializeConfig() {
        return serializeConfig;
    }

    public Retrofit2ConverterFactory setSerializeConfig(SerializeConfig serializeConfig) {
        this.serializeConfig = serializeConfig;
        return this;
    }

    public SerializerFeature[] getSerializerFeatures() {
        return serializerFeatures;
    }

    public Retrofit2ConverterFactory setSerializerFeatures(SerializerFeature[] features) {
        this.serializerFeatures = features;
        return this;
    }

    private final class ResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private Type type;

        ResponseBodyConverter(Type type) {
            this.type = type;
        }

        @Override
        public T convert(@NonNull ResponseBody value) throws IOException {
            try {
                return JSON.parseObject(value.string()
                        , type
                        , parserConfig
                        , featureValues
                        , features != null
                                ? features
                                : EMPTY_SERIALIZER_FEATURES
                );
            } finally {
                value.close();
            }
        }
    }

    private final class RequestBodyConverter<T> implements Converter<T, RequestBody> {
        RequestBodyConverter() {
        }

        @Override
        public RequestBody convert(@NonNull T value) throws IOException {
            byte[] content = JSON.toJSONBytes(value
                    , serializeConfig == null
                            ? SerializeConfig.globalInstance
                            : serializeConfig
                    , serializerFeatures == null
                            ? SerializerFeature.EMPTY
                            : serializerFeatures
            );

            return RequestBody.create(MEDIA_TYPE, content);
        }
    }
}
