roboteamlg
==========

### Left to do.
* [Fred] AntiGravityDrive: compute scalar product as to avoid escape angle to lead straight to an enemy bot.
* Spinning Radar: Adapt target locking to RoboVampires cases.
* Build a new radar that mixes Spinning and Locking.
* [Flo] List scenaris (i.e. different phases of the game)
* [Flo] Define scenario to flee to safe area.
* [Eddy] Prepare painting framework


### [Some vocabulary](https://coggle.it/diagram/51ade2c0e354014b1c00a43c/a19ae89e8368aa6171bd485adc1017fae44904e554ae9272fec52f6bb85c2294) (references robocode wiki)

### Some thoughts

* Distinguish strategies depending on gane phases:
  - Initial: explore
  - Robovamp sleeping
  - Robovamp awake
  - Robovamp killed
  - 1-vs-1

* Drive tweaks
  - Allow to be slightly nearer the robovamps if not their target
  - Allow to be nearer our target

* Gun tweaks
  - Store current target
  - Smart target change based on:
    . Type (robovamp / robovamp target / other)
    . Snapshot age
    . target angular distance to gun

