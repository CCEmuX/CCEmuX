local args = { ... }

if ccemux then
    local function help()
        print("Usages:")
        print("emu close - close this computer")
        print("emu open [id] - open another computer")
        print("emu data - opens the data folder")
        print("emu config - opens the config editor")
        --print("emu set <setting> <values> - edits a setting")
        --print("emu list settings - list editable settings")
        --print("emu save - saves current settings")
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
            if ccemux.openDataDir() then
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
        elseif args[1] == "set" then
            if #args <= 1 then
                help()
            else
                if args[2] == "resolution" then
                    if #args == 4 then
                        ccemux.setResolution(tonumber(args[3]), tonumber(args[4]))
                        print("Set resolution to " .. args[3] .. "x" .. args[4])
                    elseif #args == 3 then
                        if args[3] == "computer" then
                            ccemux.setResolution(51, 19)
                            print("Set resolution to computer (51x19)")
                        elseif args[3] == "pocket" then
                            ccemux.setResolution(26, 20)
                            print("Set resolution to pocket (26x20)")
                        elseif args[3] == "turtle" then
                            ccemux.setResolution(39, 13)
                            print("Set resolution to turtle (39x13)")
                        end
                    else
                        printError("Usage: emu set resolution <width> <height>")
                    end
                --elseif args[2] == "scale" then

                elseif args[2] == "cursor" then
                    ccemux.setCursorChar(args[3])
                    print("Set cursor char to " .. args[3])
                else
                    printError("Unrecognized setting: " .. args[2])
                end
            end
        elseif args[1] == "list" then
            if args[2] == "settings" then
                print("Editable settings:")
                print("resolution <width> <height>")
                -- not yet implemented
                --print("scale <pixels>")
                print("cursor <char>")
            else
                printError("Unrecognized subcommand: " .. args[2])
            end
        elseif args[1] == "save" then
            ccemux.saveSettings()
            print("Saved settings")
        else
            printError("Unrecognized subcommand: " .. args[1])
            help()
        end
    end
else
    printError("CCEmuX API is disabled or unavailable.")
end
