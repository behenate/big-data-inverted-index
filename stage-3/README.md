# Setting up protocol buffers (non-docker)
### 1. Installing
#### For MacOS you only have to install protocol buffers with 
`brew install protobuf`
everything should work as is.

#### Windows requires a bit more work
Download the windows version of protobuf and extract it
https://github.com/protocolbuffers/protobuf/releases/download/v29.2/protoc-29.2-win64.zip
Open the main pom.xml
Change this line:
```
<configuration>
    <protocExecutable>/opt/homebrew/bin/protoc</protocExecutable>
</configuration>
```

into something like this(change the versions of whatever I didn't test it):
```
<configuration>
    <protocArtifact>C:/Downloads/protoc29.2/bin/protoc.exe</protocArtifact>
</configuration>
```

### Compiling the proto sources
Run 

`mvn compile`

Now you can build the project as normal. All proto classes should be built.

# How to run in docker

```shell
docker compose build crawler
compose compose up crawler
```

```shell
docker compose build indexer
compose compose up indexer
```

```shell
docker compose build query-engine
compose compose up query-engine
```

## If you want to run the crawler:
- `docker compose build crawler`
- `docker compose up crawler`

Same for any other service
Running any service should start the `mongo_db` service. If it doesn't work for somer reason 
you can just run

- `docker compose build mongo_db`
- `docker compose up mongo_db`