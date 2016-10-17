local args = {...}

if ccemux then
	local function help()
		print("Example usages:")
		print("emu close - close this emulator")
		print("emu open - open an emulator with the next ID")
		print("emu open 2 - open an emulator with ID 2")
		print("emu data - opens the data folder")
	end
	
	if #args == 0 then
		help()
	else
		if args[1] == "close" then
			ccemux.closeEmu()
		elseif args[1] == "open" then
			print("Opened computer ID " .. ccemux.openEmu(tonumber(args[2])))
		elseif args[1] == "data" then
			if ccemux.openDataDir() then
				print("Opened data folder")
			else
				print("Unable to open data folder")
			end
		else
			printError("Unrecognized subcommand: " .. args[1])
			help()
		end
	end
else
	printError("CCEmuX API is disabled or unavailable.")
end