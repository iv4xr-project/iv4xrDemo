#!/usr/local/bin/python3

# See the bottom for instructions how to use

saveToFile = True

import matplotlib
if saveToFile:
   matplotlib.use('Agg')   # to generate png output, must be before importing matplotlib.pyplot

import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np
import math
import csv
import os
import sys
import pprint
import statistics
import scipy.stats as scistats

def loadCSV(csvfile):
   # need to use a correct character encoding.... latin-1 does it
   with open(csvfile, encoding='latin-1') as file:
      content = csv.DictReader(file, delimiter=',')
      rows = []
      for row in content: rows.append(row)
      return rows

#data_set1 = loadCSV("./data_set1.csv")
#data_set2 = loadCSV("./data.csv")

def mkTimeProgressionGraph(filename):

    basename = filename.rsplit('.')[0]
    dataset = data_set1 = loadCSV(filename)

    plt.ylabel('intensity')
    plt.xlabel('time')
    plt.grid(b=True, axis='y')

    plt.plot([ int(r['t']) for r in dataset ],
             [ float(r['hope']) for r in dataset ],
             label = 'hope' )

    plt.plot([ int(r['t']) for r in dataset ],
             [ float(r['fear']) for r in dataset ],
             label = 'fear' )

    plt.plot([ int(r['t']) for r in dataset ],
             [ float(r['joy']) for r in dataset ],
               label = 'joy' )

    plt.plot([ int(r['t']) for r in dataset ],
             [ float(r['satisfaction']) for r in dataset ],
               label = 'satisfaction' )

    #fig.suptitle("Emotion time progression")
    plt.title("Emotion over time in a simulated gameplay")
    plt.legend()
    if saveToFile : plt.savefig('emoOverTime_' + basename + '.png')
    else : plt.show()

def mkHeatMap(filename):

    basename = filename.rsplit('.')[0]
    dataset = data_set1 = loadCSV(filename)

    scale = 1
    width  = 12
    height = 8
    white = 45
    map = np.zeros((scale*height,scale*width))
    for x in range(0,scale*height):
      for y in range(0,scale*width):
          map[x][y] = white

    for r in dataset:
        xx = round(scale*float(r['x']))
        yy = round(scale*float(r['y']))
        # rotate +90 degree
        x = scale*height - yy
        y = xx
        hope = float(r['hope'])
        joy = float(r['joy'])
        satisfaction = float(r['satisfaction'])
        combined = 15*(hope + 1.1*joy + 1.5*satisfaction)
        if map[(x,y)]==white:
           map[(x,y)] = combined
        else:
           map[(x,y)] = max(map[(x,y)],combined)

    ax = plt.gca()
    ax.xaxis.set_visible(False)
    ax.yaxis.set_visible(False)
    plt.imshow(map, cmap='hot', origin='lower', interpolation='nearest')

    plt.title("Positive emotion heat map")
    #plt.legend()
    if saveToFile : plt.savefig('emoHeatmap_' + basename + '.png')
    else : plt.show()

def mkColdMap(filename):

    basename = filename.rsplit('.')[0]
    dataset = data_set1 = loadCSV(filename)

    scale = 1
    width  = 12
    height = 8
    white = 10
    map = np.zeros((scale*height,scale*width))
    for x in range(0,scale*height):
      for y in range(0,scale*width):
          map[x][y] = white

    for r in dataset:
        xx = round(scale*float(r['x']))
        yy = round(scale*float(r['y']))
        # rotate +90 degree
        x = scale*height - yy
        y = xx
        fear = 10*float(r['fear'])
        if map[(x,y)]== white:
           map[(x,y)] = fear
        else:
           map[(x,y)] = max(map[(x,y)],fear)

    ax = plt.gca()
    ax.xaxis.set_visible(False)
    ax.yaxis.set_visible(False)
    plt.imshow(map, cmap='hot', origin='lower', interpolation='nearest')

    plt.title("Negative emotion heat map")
    #plt.legend()
    if saveToFile : plt.savefig('emoColdmap_' + basename + '.png')
    else : plt.show()


# Available data-sets:
#    data_xxx_setup1.csv : a playthrough over a small LR level with fire
#    data_xxx_setup2.csv : a playthrough over the same LR level, with a bit more
#                      fire, and some difference in the placing of the fire
#
# To make the heat-map of combined hope-joy-satisfaction of setup2:
plt.clf()
mkHeatMap('data_goalQuestCompleted_setup2.csv')
# To make the heat-map of fear of setup2:
plt.clf()
mkColdMap('data_goalQuestCompleted_setup2.csv')
plt.clf()
mkHeatMap('data_goalGetMuchPoints_setup2.csv')
plt.clf()
mkColdMap('data_goalGetMuchPoints_setup2.csv')

# changing some plot parameters, such as font-size and width of the graph:
plt.rcParams.update({'font.size': 16})
plt.figure(figsize=(8,4.8)) # in inches; default is w=6.4 and h=4.8

# To build the graph depicting how emotions develop over time: (uncomment)
#
mkTimeProgressionGraph('data_goalQuestCompleted_setup1.csv')
plt.clf()
mkTimeProgressionGraph('data_goalQuestCompleted_setup2.csv')
#
plt.clf()
mkTimeProgressionGraph('data_goalGetMuchPoints_setup1.csv')
plt.clf()
mkTimeProgressionGraph('data_goalGetMuchPoints_setup2.csv')
