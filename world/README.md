## Importing the world

Using [Simple World Downloader](https://modrinth.com/mod/simple-world-downloader) with the target version
will get you a usable world in Anvil format. 

copy the `level.dat` and `region/*` of the map into `anvil/`

Then convert to Polar. You will probably have to fiddle with the versions of the converter dependencies.
```shell
java -jar converter/build/libs/polar-converter-1.0-SNAPSHOT-all.jar --world anvil --output lobby
```

Copy `lobby.polar` into `src/resources` and rebuild the JAR.
