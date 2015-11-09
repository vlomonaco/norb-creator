# NorbCreator #

NorbCreator is a Java tool that can be used to sequentialize [NORB](http://www.cs.nyu.edu/~ylclab/data/norb-v1.0/) (NYU Object Recognition Benchmark) images. 
It has been originally designet to suit research purposes described in this paper. 
It can also be easily expanded to manage any other dataset.

### Getting started ###
The easiest way to use the tool is to [download](https://bitbucket.org/vincenzo_lomonaco/norbcreator/src/d4a99663fbfa6526245affe03801e5f3f172c0fa/norbCreator.jar?at=master) the jar package and run it as follows:

```
java -jar norbCreator.jar configFileName

```

_configFileName_ should be a text file containing the following information:

```
#########################
#      CONFIG FILE      #
#########################
#########################
#    CONVERSION PARAMS  #
#########################
matlabFile: ../Data/Matlab/smallnorb-5x46789x9x18x6x2x96x96-training-
destDir: ../Data/all32
convert(yes/no): no
inputWidth: 96
inputHeight: 96
scaleFactor: 3
#########################
#########################
#     COMMON PARAMS     #
#########################
imagesRepo: ../Data/all32/L
nClass: 2
nObjxClass: 10
elevationProb: 0.55
azimuthProb: 0.35
lightingProb: 0.1
flipProb: 0.05
seqLen: 40
#########################
#########################
#      TRAIN PARAMS     #
#########################
fileName: train_conf.txt
nSeqxObj: 1
seed: 1
#########################
#########################
#      TEST PARAMS      #
#########################
fileName: test_conf.txt
nSeqxObj: 1
seed: 2
minDistance: 0
#########################
#########################
#      END CONFIG       #
#########################
```
You can simply Change the content as you prefer and run the jar. It will start to convert images from the matlab format and sequentialize them with respect to the submitted parameters. In the end, it will launch a GUI to visualize the sequences.

You can also run the jar, considering only a portion of the configuration file using the following command that are self-explanatory:

```
java -jar norbCreator.jar --convert configFileName

```
```
java -jar norbCreator.jar --sampleTrain configFileName

```
```
java -jar norbCreator.jar --sampleTest configFileName

```
```
java -jar norbCreator.jar --seqExplorer configFileName

```

### Recommendations ###
This software is highly experimental and comes with no warranties. It has been specifically designed to create sequences that meet the requirements and a full unit test has been created for the purpose. Other than that, the software is not that flexible and you're free and encouraged to contribute with a pull request and make it better! :-)


### Copyright ###

NorbCreator: _an easy way to sequentialize the NORB dataset_.

Copyright (C) 2015 _Vincenzo Lomonaco_, _Davide Maltoni_

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

### Authors ###

* [Vincenzo Lomonaco](http://vincenzolomonaco.com/)
* [Davide Maltoni](http://bias.csr.unibo.it/maltoni/)