
### Experiment

One small Lab Recruits level. The level goal is to reach an exit-room. To reach this room, the player has to open a door marked with 'exit' first, which in turn requires solving a puzzle involving finding and toggling a series of buttons. The is fire in the level, some cannot be avoiding. The player has to take this into account in order to solve the puzzle, and survive.

We set two variations:

1. **Setup-1**: is the baseline, representing the first attempt of the level designer.
2. **Setup-2**: has a bit more fire, but notably some of the placing is also different.

We use aplib to automatically drive a playthrough on both setups using a test agent, and use the OOC appraisal system to keep track of the agent's emotion.

### Files

* `setup1.png`, `setup1.png`: screenshots of both setups.
* `floorplan.png`: showing the floorplan of the level (the same for both setups).
* `buttons_doors_1_setup1.csv`, `buttons_doors_1_setup2.csv`: the level definition files of the setups.
* `data_setup1.csv`, `data_setup2.csv`: the resulting emotion data from the playthrough on both setups. Note that for now they only contain the data for the emotions towards the goal 'completing the level'. There is another goal, of which you can also get data. I leave this to you :)
* `mkgraph.py`: a Python3 file to build several graphs to visualize the collected data. This will create graphs showing how the emotions progress over time, and graphs showing emotion heatmaps:
  * `emoOverTime_data_setup1.png`: emotion overtime for setup-1.
  * `emoOverTime_data_setup2.png`: emotion overtime for setup-2.
  * `emoHeatmap_data_setup2.png`: the heatmap of hope+joy+satisfaction for setup2.
  * `emoColdmap_data_setup2.png`: the heatmap of fear for setup2.
* There are also heatmaps that are overlayed with the floorplan. Note that these have to be hand-made by combining the corresponding graphs manually.
