+ Array{
	selectIndices{ arg f;
		var res;
		this.do({|x,i| if(f.(x)){res=res.add(i)} });
		^res
	}
}

