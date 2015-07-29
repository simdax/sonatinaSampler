Quark to play Sonatina with Super Collider
=====

_Simple lib to use pattern with Sonatina's sounds_


1. Download the classic classical samples library @
http://sso.mattiaswestlund.net/

put it in a path you can remember.

2. copy the file "Sampler.sc" in Platform.userExtensionDir

3. init

```
(

Quarks.install("WarpExt");
Server.local.waitForBoot({
a=SonatinaSampler(); // <== path where you put sonatina samples
a.init
})
)

```

4. start hacking Hans Zimmer

```
(
Pbind(
	\type, \phrase,
	\instrument, \sonatinaTrumpets, // or \sonatinaCello \sonatinaAnotherInstrument etc.
	\mel, Pseq([60, 50, 61]),
	\dur, 1.2
).play
)
```


5. TODO 

* add percussions
* add others isntruments