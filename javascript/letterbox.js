class Configuration {

    dispatch(message) {
        if (message[0] == "game_messages")
            return this._gameMessages(message[1]);
        else if (message[0] == "boat_position")
            return this._boatPosition(message[1]);
        else if (message[0] == "shaded_cell_position")
            return this._shadedCellPosition(message[1]);
        else if (message[0] == "board_index")
            return this._boardIndex(message[1]);
        else
            throw Error("Message not understood " + message[0])
    }

    _gameMessages() {
        return new Map([
            ["fishesWin", [" Go team fish! ", " Finally free. "]],
            ["huntersWin",[" The fishing was good; it's ", " the catching that was bad. "]],
            ["tie", [" Nice tie ", " LOL! "]],
            ["missingInput", [" Missing inputs. ", " The game ends!! "]],
            ["invalidInput", [" The provided input is not valid! ", "Offending input: "]]
        ]);
    }

    _boatPosition() {
        return -1;
    }

    _shadedCellPosition() {
        return 6;
    }

    _boardIndex() {
        return 4; // index of boardLength in boardPositionsList
    }
}

class InputEvaluationManager {
    
    dispatch(message) {
        if (message[0] == "init")
            return this._init(message[1]);
        else if (message[0] == "evaluated_input_list")
            return this._evaluatedList();
        else if (message[0] == "is_invalid")
            return this._isInvalid(message[1], message[2]);
        else
            throw Error("Message not understood " + message[0])
    }

    _init(args) {
        this._evaluatedInputs = []; // class field initialization
        this._evaluatedInputs[0] = false; // default value - not invalid
        for (let i = 0; i <= args.length; i++) {
            this._evaluatedInputs[i+1] = this._isInvalid(args, i);
        }
    }

    _evaluatedList() {
        return this._evaluatedInputs;
    }

    _isInvalid(args, round) {
        if (args[round] != "1" && args[round] != "2" && args[round] != "3" && args[round] != "4" && args[round] != "5" && args[round] != "6" & round < args.length)
            return true;
    }
}

class boardManager {
    
    dispatch(message) {
        if (message[0] == "init")
            return this._init(message[1]);
        else if (message[0] == "board_positions_list")
            return this._boardPositionList();
        else
            throw Error("Message not understood " + message[0]);
    }

    _init(args) {
        // iterate through the inputs and update the positions of the fishes and board length
        this._boardPositions = []; // class field initialization
        this._boardPositions[0] = [5, 5, 5, 5, 11]; // fishes are initially located in 6th position and board length is 11
        for (let i = 0; i <= args.length; i++) {
            let currentBoardPositions = this._boardPositions[i].slice();
    
            // CHECK THE VALIDITY OF THE CURRENT INPUT
            let evaluatedInput = new InputEvaluationManager();
            if (evaluatedInput.dispatch(["is_invalid", args, i])) {
                this._boardPositions.push(currentBoardPositions);
                continue;
            }

            currentBoardPositions = this._updateCurrentBoardPositions(args[i], currentBoardPositions);   
            this._boardPositions.push(currentBoardPositions);
        }
    }

    _updateCurrentBoardPositions(dice, currentBoardPositions) {
        var conf = new Configuration();
        const BOAT_POSITION = conf.dispatch(["boat_position"]);
        const BOARD_INDEX = conf.dispatch(["board_index"]);
        // if 1/6 is drawn, the hunter boat moves OR after the hunter capture a fish, when the number to the fish is drawn
        if (dice == 1 || dice == 6 || currentBoardPositions[dice - 2] == BOAT_POSITION) {
            for (let i = 0; i < currentBoardPositions.length; i++) {
                if (currentBoardPositions[i] != BOAT_POSITION)
                    currentBoardPositions[i] -= 1;
            }
        }
        // If the number to the fish is received, then another fish can move by one step. CLOSEST TO SAFETY + SMALLEST NUMBER
        else if (currentBoardPositions[dice - 2] == currentBoardPositions[BOARD_INDEX]) {
            let minIndex = -1;
            for (let i = 3; i >= 0 ; i--) {
                if (currentBoardPositions[i] == currentBoardPositions[BOARD_INDEX])
                    continue;
                else if (minIndex == -1)
                    minIndex = i;
                else if (currentBoardPositions[i] >= currentBoardPositions[minIndex])
                    minIndex = i;
            }
            currentBoardPositions[minIndex] += 1;
        }
        else {
            currentBoardPositions[dice - 2] += 1;
        }
        return currentBoardPositions;
    }

    _boardPositionList() {
        return this._boardPositions;
    }

}

class GameStatusManager {
    
    dispatch(message) {
        if (message[0] == "check_game_status")
            return this._checkGameStatus(message[1], message[2], message[3])
        else
            throw Error("Message not understood " + message[0]);
    }

    _checkGameStatus(currentBoardPositions, round, inputsLength) {
        var conf = new Configuration();
        const BOAT_POSITION = conf.dispatch(["boat_position"]);
        const BOARD_INDEX = conf.dispatch(["board_index"]);

        let freeFish = 0;
        let caughtFish = 0;
        for (let i = 0; i < currentBoardPositions.length - 1; i++) {
            if (currentBoardPositions[i] == BOAT_POSITION)
                caughtFish += 1;
            else if (currentBoardPositions[i] == currentBoardPositions[BOARD_INDEX])
                freeFish += 1;
        }
        if (round > inputsLength)
            return "missingInput";
        if (freeFish + caughtFish < 4)
            return "gameContinue"; // STILL FISH IN RIVER
        if (freeFish >= 3) 
            return "fishesWin";
        if (caughtFish >= 3) 
            return "huntersWin";
        if (freeFish == 2 && caughtFish == 2)
            return "tie";
    }

}

class MessageBoxManager {
    
    dispatch(message) {
        if (message[0] == "prepare_message_content")
            return this._prepareMessageContent(message[1], message[2], message[3], message[4]);
        else if (message[0] == "calculate_margin")
            return this._calculateMargin(message[1], message[2]);
        else if (message[0] == "draw_message_box")
            return this._drawMessageBox(message[1], message[2]);
        else
            throw Error("Message not understood " + message[0]);
    }

    // PREPARING MESSAGE CONTENT
    _prepareMessageContent(boardLength, gameStatus, input, invalidInput) {
        var conf = new Configuration();
        const MESSAGES = conf.dispatch(["game_messages"]);
        let actualBoardLengths = this._actualBoardLength(boardLength);
        let selectedMessage = (invalidInput ? MESSAGES.get("invalidInput") : MESSAGES.get(gameStatus));
        let messageContent = []; // containing the content of two lines of messages
        
        for (let i = 0; i <= 1; i++) {
            let message = selectedMessage[i];
            let messageBoxSize;
            if (invalidInput && i == 1) { // APPEND INVALID INPUT IN MESSAGE
                message += (input.length > 3) ? input.substring(0, 3) + "..." : input;
                messageBoxSize = (actualBoardLengths - selectedMessage[0].length < 0) ? selectedMessage[0].length - message.length: actualBoardLengths - message.length;
            }else
                messageBoxSize = actualBoardLengths - message.length;
            messageBoxSize = (messageBoxSize < 0 ? 0 : messageBoxSize);
            let margin = parseInt(messageBoxSize / 2);
            messageContent[i] = "│";
            messageContent[i] += " ".repeat(messageBoxSize % 2 != 0 ? margin+1 : margin); // In case the message cannot be perfectly centered
            messageContent[i] += message;
            messageContent[i] += " ".repeat(margin);
            messageContent[i] += "│";
        }
        return messageContent;
    }

    // CALCULATING MARGIN REQUIRED FOR ALIGNMENT
    _calculateMargin(messageContentLength, boardLength) {
        let margin = "";
        let marginLength = parseInt((messageContentLength - this._actualBoardLength(boardLength)) / 2);
        for (let i = 0; i < marginLength; i++) {
            margin += " ";
        }
        return margin;
    }

    // DRAWING MESSAGE BOX/BANNER
    _drawMessageBox(currentBoard, messageContent) {
        let boardCreationManager = new BoardCreationManager();
        currentBoard.push(boardCreationManager.dispatch(["draw_outside_boarder", true, messageContent[0].length-2, "", false]));
        currentBoard.push(messageContent[0]);
        currentBoard.push(messageContent[1]);
        currentBoard.push(boardCreationManager.dispatch(["draw_outside_boarder", false, messageContent[0].length-2, "", false]));
        return currentBoard;
    }

    _actualBoardLength(boardLength) {
        // based on the number of characters 
        return 9 + 9 + 4 * boardLength;
    }
}

class BoardCreationManager {
    
    dispatch(message) {
        if (message[0] == "draw_outside_boarder")
            return this._drawOutsideBoarder(message[1], message[2], message[3], message[4]);
        else if (message[0] == "draw_four_middle_line")
            return this._drawFourMiddleLines(message[1], message[2], message[3]);
        else
            throw Error("Message not understood " + message[0]);
    }

    // DRAWING TOP/BOTTOM LINE
    _drawOutsideBoarder(topLine, boardLength, margin, thickLine) {
        let line;
        if (thickLine)
            line = topLine ? "╔════════╤" : "╚════════╧";
        else
            line = topLine ? "┌" : "└";
        for (let j = 0; j < boardLength; j++) {
            if (thickLine)
                line += topLine ? "═══╤" : "═══╧";
            else
                line += "─";
        }
        if (thickLine)
            line += topLine ? "═══════╗" : "═══════╝";
        else
            line += topLine ? "┐" : "┘";

        return margin + line + margin;    
    }

    // DRAWING FOUR MIDDLE LINE; BOARD ONLY
    _drawFourMiddleLines(currentBoard, currentBoardPosition, index) {
        var conf = new Configuration();
        const BOAT_POSITION = conf.dispatch(["boat_position"]);
        const BOARD_INDEX = conf.dispatch(["board_index"]);
        const SHADED_CELL_POSITION = conf.dispatch(["shaded_cell_position"]);

        for (let i = 0; i < 4; i++) {
            //////// draw boat section
            let boat = "";
            switch (i) {
                case 0:
                    boat = "┌──┐1";
                    break;
                case 1:
                case 2:
                    let fishIndex = (i == 1 ? 1 : 3);
                    boat = "│" + (currentBoardPosition[index][fishIndex-1] == BOAT_POSITION ? fishIndex+1 : " ");
                    boat += (currentBoardPosition[index][fishIndex] == BOAT_POSITION ? fishIndex+2 : " ") + "│ ";
                    break;
                case 3:
                    boat = "└──┘6";
                    break;
            }
            let line = "║  " + boat + " │";
            //////// draw river section
            for (let j = 0; j < currentBoardPosition[index][BOARD_INDEX]; j++) {
                if (j == currentBoardPosition[index][BOARD_INDEX] - SHADED_CELL_POSITION && j != currentBoardPosition[index][i])
                    line += " ▓ │";
                else if (j == currentBoardPosition[index][i])
                    line += " " + (i + 2) + " │";
                else
                    line += "   │";
            }
            //////// draw sea section
            if (currentBoardPosition[index][i] == currentBoardPosition[index][BOARD_INDEX])
                line += "   " + (i + 2) + "   ║";
            else
                line += "       ║";
            currentBoard.push(line);
        }
        return currentBoard;
    }
}

class GameController {

    dispatch(message) {
        if (message[0] == "init")
            return this._init(message[1]);
        else if (message[0] == "run")
            return this._run(message[1])
        else
        throw Error("Message not understood " + message[0]);
    }
 
    _init(args) {
        this._inputEvaluationManager = new InputEvaluationManager();
        this._boardManager = new boardManager();
        this._gameStatusManager = new GameStatusManager();

        this._messageBoxManager = new MessageBoxManager();
        this._boardCreationManager = new BoardCreationManager();

        this._inputEvaluationManager.dispatch(["init",args])
        this._boardManager.dispatch(["init",args])
    }

    _run(args) {
        let conf = new Configuration();
        const BOARD_INDEX = conf.dispatch(["board_index"]);
        let evaluatedInputs = this._inputEvaluationManager.dispatch(["evaluated_input_list"]);
        let boardPositions = this._boardManager.dispatch(["board_positions_list"]);
        let margin = "";        

        for (let i = 0; i <= args.length + 1; i++) {            
            let currentBoard = [];
            let gameStatus = this._gameStatusManager.dispatch(["check_game_status", boardPositions[i], i, args.length]);
            let messageShown = evaluatedInputs[i] || gameStatus != "gameContinue";
            let messageContent = ["", ""]; // containing the content of two lines of messages
            let margin = ""; // using for alignment when messages required to be shown

            if (messageShown) {
                messageContent = this._messageBoxManager.dispatch(["prepare_message_content", boardPositions[i][BOARD_INDEX], gameStatus, args[i-1], evaluatedInputs[i]]);
                margin = this._messageBoxManager.dispatch(["calculate_margin", messageContent[0].length, boardPositions[i][BOARD_INDEX]]);
            }

            // DRAWING TOP BOARDER
            currentBoard.push(this._boardCreationManager.dispatch(["draw_outside_boarder", true, boardPositions[i][BOARD_INDEX], margin, true]));
            // DRAWING FOUR MIDDLE LINES
            if (messageShown)
                currentBoard = this._messageBoxManager.dispatch(["draw_message_box", currentBoard, messageContent]);
            else
                currentBoard = this._boardCreationManager.dispatch(["draw_four_middle_line", currentBoard, boardPositions, i]);
            // DRAWING BOTTOM BOARDER
            currentBoard.push(this._boardCreationManager.dispatch(["draw_outside_boarder", false, boardPositions[i][BOARD_INDEX], margin, true]));

            currentBoard.forEach(board => {
                console.log(board);
            });
            if (gameStatus != "gameContinue")
                break;
        }
  
    }
}


let main = function(args) {

    let gameController = new GameController()
    gameController.dispatch(['init', args])
    gameController.dispatch(['run', args])

}

if (require.main === module) {
    var args = process.argv.slice(2);
    main(args);
}

// Export the main function so it can be called from the tests
module.exports.main = main
