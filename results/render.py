#!/usr/bin/python
# -*- coding: utf8 -*-

from glob import glob
import json
import pygal
from pygal.style import CleanStyle as TheStyle


def read_results(fnames):
	results = []
	for fname in fnames:
		f = open(fname)
		results.append(json.loads(f.read()))
		f.close()
	return results

def process_result(result, queueOut, blazeOut):
	for datapoint in result:
		r = {
			'threads': datapoint['threads'],
			'score': datapoint['primaryMetric']['score'],
			'confidence': datapoint['primaryMetric']['scoreConfidence']
		}
		if 'Queue' in datapoint['benchmark']:
			queueOut.append(r)
		else:
			blazeOut.append(r)

queue21 = []
blaze21 = []
queue22 = []
blaze22 = []

for result in read_results(glob('semi-stormpot-2.1/*')):
	process_result(result, queue21, blaze21)

for result in read_results(glob('semi-stormpot-2.2/*')):
	process_result(result, queue22, blaze22)

labels = [str(datapoint['threads']) for datapoint in queue21]

def score(datapoint):
	return datapoint['score']

line_chart = pygal.Line(style=TheStyle)
line_chart.title = 'Throughput'
line_chart.x_labels = labels
line_chart.x_title = 'Threads'
line_chart.y_title = 'claim/release cycles per microsecond'
line_chart.add('QueuePool 2.2', map(score, queue22))
line_chart.add('QueuePool 2.1', map(score, queue21))
line_chart.add('BlazePool 2.2', map(score, blaze22))
line_chart.add('BlazePool 2.1', map(score, blaze21))
line_chart.render_to_file('stormpot-2.2-throughput.svg')

