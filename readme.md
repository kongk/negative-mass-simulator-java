This project is currently a work-in-progress.

It is java port of the project
https://github.com/jamiefarnes/negative-mass-simulator

but with some modifications in the simulation algorithm which results in significant quicker execution.

Also the viewer part is different, as a 3D viewer is used instead of images and video.

Iteration times seems to be somewhat faster than that of the original python program, especially for 50000+ particles.

Initial invocations seems to confirm the findings in the original program.

### Notes

In order to run on MacOS Tahoe 26.2, I recommend using e.g. Oracle OpenJdk 21.0.9 (21.0.9-oracle), as the 3D viewer
software library is crashing when run on e.g. amazon coretto, both 21 and 25 (21.0.9-amzn and 25.0.1-amzn) 