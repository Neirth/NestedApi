/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package io.neirth.nestedapi.Users.Schemas;

@org.apache.avro.specific.AvroGenerated
public interface CreateUser {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"CreateUser\",\"namespace\":\"io.neirth.nestedapi.Users.Schemas\",\"types\":[{\"type\":\"record\",\"name\":\"UserObj\",\"fields\":[{\"name\":\"id\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"name\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"surname\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"email\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"password\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"telephone\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"birthday\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"country\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"address\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"addressInformation\",\"type\":[\"null\",\"string\"],\"default\":null}]},{\"type\":\"record\",\"name\":\"Request\",\"fields\":[{\"name\":\"id\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"user\",\"type\":[\"null\",\"UserObj\"],\"default\":null},{\"name\":\"token\",\"type\":[\"null\",\"string\"],\"default\":null}]},{\"type\":\"record\",\"name\":\"Response\",\"fields\":[{\"name\":\"status\",\"type\":[\"int\"]},{\"name\":\"message\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"object_id\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"object\",\"type\":[\"null\",\"boolean\",\"UserObj\"],\"default\":null}]}],\"messages\":{\"CreateUser\":{\"request\":[{\"name\":\"request\",\"type\":\"Request\"}],\"response\":\"Response\"}}}");
  /**
   */
  io.neirth.nestedapi.Users.Schemas.Response CreateUser(io.neirth.nestedapi.Users.Schemas.Request request);

  @SuppressWarnings("all")
  public interface Callback extends CreateUser {
    public static final org.apache.avro.Protocol PROTOCOL = io.neirth.nestedapi.Users.Schemas.CreateUser.PROTOCOL;
    /**
     * @throws java.io.IOException The async call could not be completed.
     */
    void CreateUser(io.neirth.nestedapi.Users.Schemas.Request request, org.apache.avro.ipc.Callback<io.neirth.nestedapi.Users.Schemas.Response> callback) throws java.io.IOException;
  }
}