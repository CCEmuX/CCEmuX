local args = { ... }

if ccemux then
    local function help()
        print("Usages:")
        print("emu close - close this computer")
        print("emu open [id] - open another computer")
        print("emu data [id] - opens the data folder")
        print("emu config - opens the config editor")
        print("Run 'help emu' for additional information")
    end

    if #args == 0 then
        help()
    else
        if args[1] == "close" then
            ccemux.closeEmu()
        elseif args[1] == "open" then
            print("Opened computer ID " .. ccemux.openEmu(tonumber(args[2])))
        elseif args[1] == "data" then
            local id = nil
            if args[2] ~= nil then
                id = tonumber(args[2])
                if id == nil then
                    printError("Expected a computer ID")
                    return
                end
            end
            if ccemux.openDataDir(id) then
                print("Opened data folder")
            else
                print("Unable to open data folder")
            end
        elseif args[1] == "config" then
            local ok, err = ccemux.openConfig()
            if ok then
                print("Opened config editor")
            else
                print(err)
            end
        else
            printError("Unrecognized subcommand: " .. args[1])
            help()
        end
    end
else
    printError("CCEmuX API is disabled or unavailable.")
end
