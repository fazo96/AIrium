# AIrium

![AIrium Short GIF](http://i.imgur.com/OTkPsSC.gif)

This project aims to build an interactive simple artificial life simulation that uses genetic algorithm and fitness evaluation to improve, over different generations, the performance of the agents.

For now, agents are evaluated based on how much they eat. Using default settings you can see them evolve to be herbivores, but by tweaking it is possible to get carnivores or scavengers.

__In the GIF__ you can see an environment similar to the default, but smaller. Most creatures are pretty efficient herbivores with a few showing slight carnivore behavior. This was obtained in only 5 generations of training, in about two minutes on an average CPU. __You can see a GIF with a totally different outcome [here](http://www.gfycat.com/MilkyFloweryImperatorangel)__, it shows some creatures which evolved to eat food by stopping when they encounter it and always travelling in a straight line. This was obtained in a little more time with a slightly different configuration, showing that a lot can change just by running different simulation even with the same settings.

__Since version 0.5__ it is possible to __save your best creatures to disk__ so you can reintroduce them to different environments later and avoid losing very efficient agents.

### Features

- __Artifical Life__ creatures with simple brains
- Artificial selection to ensure the evolution of the creatures over time
- Many __configurable settings__ to create different environments (still a little limited, but I'm working on it)
- __Easy to use__ with working default settings for beginners, __highy configurable__ and fully open source for hackers!
- __Save and Load__ your configuration and your creatures
- Every creature has a unique, persistent name generated using its brain topology, so on every computer the same creature will have the same name
- Multithread design, resizeable window, configurable simulation speed, __multiplatform support, free and open source!__

### Screenshots and videos

You can see some footage from [version 0.4](https://github.com/fazo96/AIrium/releases/tag/v0.4.0) in this [imgur album](http://imgur.com/a/27g9l)

### Download

Visis the [releases page](https://github.com/fazo96/AIrium/releases) to [download the latest version](https://github.com/fazo96/AIrium/releases/latest). It was tested on Linux, OSX and Windows using the latest version of Java 8. On OSX there is a small bug in the GUI but you can fix it by resizing it to be bigger.

### Planned features

- More behavioral variety
- More sensors for the creatures
- Ability to save and load simulation states
- More in the [issues page](https://github.com/fazo96/AIrium/issues)

### Controls

- The __Configuration__ page in the GUI will let you tweak many parameters and pause the simulation
- You can drag with the mouse to move the camera and zoom with the mouse wheel

## Development

To work on the project or try it out, install __Netbeans__ and follow _Libgdx_'s guide to set it up for [Libgdx](http://libgdx.badlogicgames.com/) development (mobile platform are ignored for now so you won't need the Android SDK). It should be possible to work on the project without using Netbeans. You can find further explanation in [Libgdx](http://libgdx.badlogicgames.com/)'s wiki.

## License

Copyright (c) 2015 Enrico Fasoli (fazo96)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
