Quark to play Sonatina with Super Collider
=====

Simple lib to use pattern with Sonatina's sounds.

```
(

Quarks.install("WarpExt");
Server.local.waitForBoot({
a=SonatinaSampler(); // <== path where you put sonatina samples
a.init
})
)
(
Pbind(
	\type, \phrase,
	\instrument, \sonatinaTrumpets, // or \sonatinaCello \sonatinaAnotherInstrument etc.
	\mel, Pseq([60, 50, 61]),
	\dur, 1.2
).play
)
```
