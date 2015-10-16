# decody-simple-json-client
Decody Simple JSON Client

### 1. Overview

Decody Simple JSON Client tries to be a small library for Android applications with the idea to let developers to do JSON calls to backend servers.

### 2. Quick start

To perform a GET call:

JSONClient client = new JSONClient();
MyObject response = client.get("http://mydomain.com/mygetservice", MyObject.class);

The JSON response from your backend will be deserialized to 'response' instance of MyObject class.

To perform a POST call:

MyObject myPostData = new MyObject();
myPostData.setMyProperty("myValue");

JSONClient client = new JSONClicent();
MyObject response = client.post("http://mydomain.com/mypostservice", MyObject.class, myPostData);

Note for that call that the class type of the post data and response are the same.

### 3. Dependencies

Dependencies for the library are listed below:

- GSON library.
