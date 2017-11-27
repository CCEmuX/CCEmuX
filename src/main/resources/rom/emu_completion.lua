-- Setup completion functions
local function completeMultipleChoice(text, options, addSpaces)
    local tResults = {}
    for n = 1, #options do
        local sOption = options[n]
        if #sOption + (addSpaces and 1 or 0) > #text and sOption:sub(1, #text) == text then
            local sResult = sOption:sub(#text + 1)
            if addSpaces then
                table.insert(tResults, sResult .. " ")
            else
                table.insert(tResults, sResult)
            end
        end
    end
    return tResults
end

local commands = { "close", "open", "data", "config" }
shell.setCompletionFunction("rom/programs/emu.lua", function(shell, index, text, previous)
    if index == 1 then
        return completeMultipleChoice(text, commands, true)
    end
end)
