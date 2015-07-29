SonatinaSampler{

	var <>sonatinaPath,
	 <>noteMin=2, <>noteMax=130, <>dics;

	*new{
		arg path=(Platform.userHomeDir+/+"freesound/samples/");
		^super.newCopyArgs(path);
	}

	init{
		// folders in sonatinaPath
		var instruments=PathName(sonatinaPath).entries;
		dics=Dictionary.new;

		// for each folder, create a dictionary of pitched buffers
		//
		instruments.collect({ |i|
			var dic;
			var a=i.folderName;

			//for the moment I remove Percussions folder because I don't
			// really now how to manage it :)
			if( (a=="Percussion"),
				{nil},
				{
					dic=this.collectSamples(i.folderName);
					dics.performList(\put, dic.asKeyValuePairs);
				}
			);
		});

		// SynthDef and Pdef to use them with name
		// sonatina+folderName
		// e.g. Pdef(\sonatinaCello)

		SynthDef(\sonatina, {arg buffer, dur=1, midinote=1, out=0;
			var sig, env;
			env = EnvGen.kr(Env([0.01, 1, 1, 0.01], [0.1, dur, 0.3], 'exp'), doneAction: 2);
			sig = Warp0.ar(2, buffer, dur, 1*midinote.midiratio, 0.1, -1, 8, 0.05, 4);
			Out.ar(out, sig*env);
		}).send(Server.local);

		dics.keys.collect({ |k|
			this.initPattern(k)
		})
	}

	// function for extract pitch from name
	// the name should be instName-noteOctave
	// a.k.a   cello-c2    or  soubassophone-f#5   etc.

	extractPitch { arg a;
		var oct=(a.last.asString.asInt +1) *12;
		var note=a.drop(-1);
		^switch(note,
			"c", 0,
			"c#", 1,
			"d", 2,
			"d#", 3,
			"e", 4,
			"f", 5,
			"f#", 6,
			"g", 7,
			"g#", 8,
			"a", 9,
			"a#", 10,
			"b", 11, )
		+oct;
	}

	// function for getting a list of Buffers from a folders filled
	// with notes Samples.
	// like /Cello/cello-a5.wav,cello-c6.wav etc.

	// it returns a dictionary such as
	// (midiPitch -> buffer)

	collectSamples { arg name;

		var p=sonatinaPath;
		var files=PathName(p +/+ name.capitalize).entries;

		// sorting by function extractPitch, i.e. by pitch
		var pitches=files.collect({ |j|
			var a= j.fileNameWithoutExtension
			.split($-).last;
			this.extractPitch(a)
		});

		//load Buffers
		var buffersPath= files.atAll(pitches.order).collect(_.absolutePath);

		// cannot use allocConsecutive since I don't know the numframe...
		var sounds=Dictionary.new;
		pitches.sort;
		buffersPath.size.do({ |k|
			sounds.add(
				pitches[k] ->
					Buffer.read(Server.local, buffersPath[k]);
			);
		});

		// returns a Dictionary such as
		//  (name  -> dic (withNotesDistributed) )
		^Dictionary.newFrom(
			[ name.asSymbol, this.distribNotes(sounds, noteMin, noteMax)]
		);
	}

	// function for evaluating best distribution
	// between buffers
	// it somewhat private

	distribNotes { arg dic, noteMin=2, noteMax= 130; // is that reasonnable ? ;)

		// check the values between each value
		var keys=dic.keys.asSortedList;
		var sortedKeys=keys.differentiate.drop(-1)-1;

		// partition this "holes" in two
		var distrib=sortedKeys.collect(_.partition(2));

		// create an Array with midinote distributed
		var result= (1..keys.size-2).collect({ |i|
			[
				keys[i] - (1 ! distrib[i-1][1]),
				keys[i] + (1 ! distrib[i][0])
			].insert(1, keys[i]).unbubble(1);
		});

		// we introduce the extreme values
		result=[(noteMin..keys.first)] ++ result ++ [(keys.last .. noteMax)];

		//and output an array of form:
		// [ notes, bufferToUse, OriginalPitch]

		^dic.atAll(keys.asSortedList)
		.collect({ |buf, index|
			[ result[index], buf, dic.findKeyForValue(buf) ]
		})

	}

	// Function for creating a Synth who plays according to samples

	// It creates also a pattern that you can use with a phrase type
	// Pbind for play the Synth


	initPattern { arg name;

		Pdef(("sonatina"++name).asSymbol, { arg mel;
			var dic=dics[name.asSymbol];

			//evaluate better sample buffer to pick up :
			var index=dic.detectIndex({ |i|
				i[0].includes(mel);
			});
			var buffer=dic[index][1].query; // dunno why, but need query...
			var midi=dic[index][2] - mel;
			 Pbind(
			 	\instrument, \sonatina,
			 	\buffer, buffer.bufnum,
			 	\midinote, midi,
			).trace;
		});
	}


}
