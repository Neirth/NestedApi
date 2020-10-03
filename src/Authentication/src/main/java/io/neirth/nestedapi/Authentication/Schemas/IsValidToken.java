/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package io.neirth.nestedapi.Authentication.Schemas;

@org.apache.avro.specific.AvroGenerated
public interface IsValidToken {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"IsValidToken\",\"namespace\":\"io.neirth.nestedapi.Authentication.Schemas\",\"types\":[{\"type\":\"record\",\"name\":\"UserObj\",\"fields\":[{\"name\":\"id\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"name\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"surname\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"password\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"telephone\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"birthday\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"country\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"address\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"addressInformation\",\"type\":[\"null\",\"string\"],\"default\":null}]},{\"type\":\"record\",\"name\":\"Request\",\"fields\":[{\"name\":\"id\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"user\",\"type\":[\"null\",\"UserObj\"],\"default\":null},{\"name\":\"token\",\"type\":[\"null\",\"string\"],\"default\":null}]},{\"type\":\"record\",\"name\":\"Response\",\"fields\":[{\"name\":\"status\",\"type\":[\"int\"]},{\"name\":\"object\",\"type\":[\"null\",\"boolean\",\"UserObj\"],\"default\":null},{\"name\":\"message\",\"type\":[\"null\",\"string\"],\"default\":null}]}],\"messages\":{\"IsValidToken\":{\"request\":[{\"name\":\"request\",\"type\":\"Request\"}],\"response\":\"Response\"}}}");
  /**
   */
  io.neirth.nestedapi.Authentication.Schemas.Response IsValidToken(io.neirth.nestedapi.Authentication.Schemas.Request request);

  @SuppressWarnings("all")
  public interface Callback extends IsValidToken {
    public static final org.apache.avro.Protocol PROTOCOL = io.neirth.nestedapi.Authentication.Schemas.IsValidToken.PROTOCOL;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void IsValidToken(io.neirth.nestedapi.Authentication.Schemas.Request request, org.apache.avro.ipc.Callback<io.neirth.nestedapi.Authentication.Schemas.Response> callback) throws java.io.IOException;
  }
}