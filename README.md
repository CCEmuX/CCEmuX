# CCEmuX
A new open source CC emulator, written with [xtend](https://www.eclipse.org/xtend/).

## Building
As simple as running `./gradlew build`.

## Configuration
CCEmuX is highly configurable through the properties file and command line arguments. Run CCEmuX with the `--help` argument to see available command line options, which let you change things like the data directory and rendering method.

To change the CCEmuX properties file, first start CCEmuX and then run `emu data` in the emulator. From the folder that opens, edit `ccemux.properties`. The options are all commented and relatively easy to use. Note that you will need to restart CCEmuX for any changes to take effect.

## Changing CC version
From the properties file you can also control what version of CC is used. For compatibility reasons these options are not automatically generated and must be added manually.

If you want to use a custom version of CC, follow the steps below. Note that using custom CC versions is not officially supported at this time and may cause errors.

1. Download the new CC jar into the CCEmuX data folder. Make sure it is named similarly to `ComputerCraft___.jar`.

2. Add the following two lines to the bottom of the CCEmuX configuration file to prevent CCEmuX from attempting to verify and update CC.
```
ccChecksum=
ccRemote=
```

3. Modify and add the following line to the bottom of the CCEmuX configuration file to specify the version of CC in use. MAKE SURE that this matches the end of the filename of the CC jar you downloaded.
```
ccRevision=1.79
```

4. Save the modified configuration file and restart CCEmuX, preferably from a terminal to see log messages. 


## About Xtend
Most of you reading this probably haven't heard of [xtend](https://www.eclipse.org/xtend/) before. It's a language that compiles to Java, that adds lots of useful features such as extension methods, operator overloading, getters/setters, better type inference, and some syntactic sugar. If you look through the source code, it's easy enough to read it just knowing Java, but if you want to learn more about it check out the [documentation](https://www.eclipse.org/xtend/documentation/index.html)