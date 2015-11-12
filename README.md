# NorbCreator #

NorbCreator is a Java tool that can be used to sequentialize [NORB](http://www.cs.nyu.edu/~ylclab/data/norb-v1.0/) (NYU Object Recognition Benchmark) images. 
It has been originally designe to suit research purposes described in the paper ["Semi-supervised Tuning from Temporal Coherence"](http://arxiv.org/abs/1511.03163) authored Maltoni, D & Lomonaco, V. 
It can also be easily expanded to manage any other image dataset.

**Please, cite the paper above if you want to use this tool.**

### Getting started ###
The easiest way to use the tool is to [download](https://bitbucket.org/vincenzo_lomonaco/norbcreator/src/d4a99663fbfa?at=master) the jar package and run it as follows:

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

### Built-in Datasets ###
Inside the repository you can find and download the training and test sets used in the paper. They are plain text files containing the NRB sequences, so you can parse them at your wish or use the code already provided in this tool.

They comes with the name train_conf.txt, test_conf2.txt, test_conf3.txt, test_conf4.txt. 
where test_confX.txt stands for a test set with mindist = X (see the paper for further details).

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