function main(factionlibrary, player)
	flags=player:getFlags();
	var=flags:readFlag("minyos_monsterhunt_0")
	if var>0 and var<5 then
		flags:incrementFlag("minyos_monsterhunt_0");
		if (var==3) then
		player:addJournal("minyos","hunt1")
		end
	end
end