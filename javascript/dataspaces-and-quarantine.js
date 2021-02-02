class PHQuarantine {
    constructor(func) {
        this._funcs = [func];
    }

    bind(func) {
        this._funcs.push(func);
        return this;
    }

    execute() {

        let guardCallable = function(value) {
            if (typeof value === "function") {
                return value();
            }
            else
                return value;
        }

        // PRINT BOARDS TO CONSOLE
        let printOutput = function(output) {
            for(let i = 0; i < output.length; i++) {
                for (j = 0; j < output[i].length; j++) {
                    console.log(output[i][j]);
                }
            }
        }

        let value = () => null;

        // this recursive function loops through _funcs
        let executeLoop = async function(funcs) {
            if (funcs.length == 0) {
                printOutput(guardCallable(value));
                return;
            }
            else {
                let guardResult = guardCallable(value);
                // if the function is async, waiting for the promises (synchronization)
                if (Promise.resolve(guardResult) == guardResult) {
                    await guardResult.then(values => {
                        value = funcs[0](guardCallable(values));
                        executeLoop(funcs.slice(1));
                    });
                } else {
                    value = funcs[0](guardResult);
                    await executeLoop(funcs.slice(1));
                }
            }
        }

        return executeLoop(this._funcs) // return promise to main function for testing to wait/sync
    }
}

/////////////////////////////////
//        The "functions"      //
/////////////////////////////////

function getInput(dummyArgs) {
    return function _f() {
        return global.ARGS;
    }
}

function inputEvaluation(args) {
    return async function _f() {
        let { Worker, workerData } = require('worker_threads');
        let promises = [];
        let splitedArgsSpace = []; // this acts as a dataspace from which the workers SENSE/READ data
        let evaluatedInputsSpace = []; // this is also a dataspace in which workers PUT data
        let inputs = args.slice();
        const NUMBER_OF_WORKERS = 3;
        
        for (let i = NUMBER_OF_WORKERS; i > 0; i--) {
            splitedArgsSpace.push(args.splice(0, Math.ceil(args.length / i)));
        }
        
        for(let i = 0; i < NUMBER_OF_WORKERS; i++) {
            const promise = new Promise((resolve, reject) => {
                let worker = new Worker(`
                var { workerData, parentPort } = require('worker_threads');
                let evaluatedInputsSpace = [];
                let worker_id = workerData[1];            
                for (let i = 0; i <= workerData.length - 1; i++) {
                    if (workerData[i] != "1" && workerData[i] != "2" && workerData[i] != "3" && workerData[i] != "4" && workerData[i] != "5" && workerData[i] != "6" & i < workerData.length)
                        evaluatedInputsSpace.push(true);
                    else
                        evaluatedInputsSpace.push(false);
                }
                parentPort.postMessage({ evaluatedInputsSpace: evaluatedInputsSpace});
                `, {eval: true, workerData: splitedArgsSpace[i]});
                worker.on('message', (msg) => {
                    resolve(msg.evaluatedInputsSpace);
                })                        
            });
            promises.push(promise);
        }
        await Promise.all(promises).then((evaluatedInputs) => {
            // since in worker_threads the only way to communicate with main thread is via messaging, the data is collected by these concatenations.
            evaluatedInputsSpace = [false].concat(evaluatedInputs[0]).concat(evaluatedInputs[1]).concat(evaluatedInputs[2]).concat(false);
        });
        return [inputs, evaluatedInputsSpace];
    }
}

function boardPositions(inputObject) {
    let args = inputObject[0];
    let evaluatedInputs = inputObject[1];
    let boardPositionList = []; // class field initialization
    boardPositionList[0] = [5, 5, 5, 5, 11]; // fishes are initially located in 6th position and board length is 11
    for (let i = 0; i <= args.length; i++) {
        let currentBoardPositions = boardPositionList[i].slice();
        // CHECK THE VALIDITY OF THE CURRENT INPUT
        if (evaluatedInputs[i+1]) {
            boardPositionList.push(currentBoardPositions);
            continue;
        }
        const BOAT_POSITION = -1;
        const BOARD_INDEX = 4;
        // if 1/6 is drawn, the hunter boat moves OR after the hunter capture a fish, when the number to the fish is drawn
        if (args[i] == 1 || args[i] == 6 || currentBoardPositions[args[i] - 2] == BOAT_POSITION) {
            for (let i = 0; i < currentBoardPositions.length; i++) {
                if (currentBoardPositions[i] != BOAT_POSITION)
                    currentBoardPositions[i] -= 1;
            }
        }
        // If the number to the fish is received, then another fish can move by one step. CLOSEST TO SAFETY + SMALLEST NUMBER
        else if (currentBoardPositions[args[i] - 2] == currentBoardPositions[BOARD_INDEX]) {
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
        else if (args[i]){
            currentBoardPositions[args[i] - 2] += 1;
        }
        boardPositionList.push(currentBoardPositions);
    }
    return [args, evaluatedInputs, boardPositionList];
}

function gameStatus(inputObject) {
    let args = inputObject[0];
    let evaluatedInputs = inputObject[1];
    let boardPositionList = inputObject[2];
    let gameStatusesList = [];
    const BOAT_POSITION = -1;
    const BOARD_INDEX = 4;
    for (let i = 0; i < boardPositionList.length; i++) {
        let freeFish = 0;
        let caughtFish = 0;
        for (let j = 0; j < boardPositionList[i].length - 1; j++) {
            if (boardPositionList[i][j] == BOAT_POSITION)
                caughtFish += 1;
            else if (boardPositionList[i][j] == boardPositionList[i][BOARD_INDEX])
                freeFish += 1;
        }
        if (i > args.length)
            gameStatusesList.push("missingInput");
        else if (freeFish + caughtFish < 4)
            gameStatusesList.push("gameContinue");
        else if (freeFish >= 3)
            gameStatusesList.push("fishesWin");
        else if (caughtFish >= 3)
            gameStatusesList.push("huntersWin");
        else if (freeFish == 2 && caughtFish == 2)
            gameStatusesList.push("tie");

        if (gameStatusesList[i] != "gameContinue")
            break;
    }
    return [args, evaluatedInputs, boardPositionList, gameStatusesList];
}

function boardCreation(inputObject) {
    let args = inputObject[0];
    let evaluatedInputs = inputObject[1];
    let boardPositionList = inputObject[2];
    let gameStatusesList = inputObject[3];
    let boards = [];
    const BOAT_POSITION = -1;
    const BOARD_INDEX = 4;
    const SHADED_CELL_POSITION = 6;
    const MESSAGES = new Map([
        ["fishesWin", [" Go team fish! ", " Finally free. "]],
        ["huntersWin",[" The fishing was good; it's ", " the catching that was bad. "]],
        ["tie", [" Nice tie ", " LOL! "]],
        ["missingInput", [" Missing inputs. ", " The game ends!! "]],
        ["invalidInput", [" The provided input is not valid! ", "Offending input: "]]
    ]);

    for (let i = 0; i < gameStatusesList.length; i++) {
        let currentBoard = [];
        let boardCounter = 0;
        let leftMargin = ""; // using for alignment when messages required to be shown
        let messageBox = []; // containing the content of two lines of messages

        if (evaluatedInputs[i] || gameStatusesList[i] != "gameContinue") {
            let actualboardLengths = 9 + 9 + 4 * boardPositionList[i][BOARD_INDEX]; // based on the number of characters
            // preparing lines corresponding to the fourth and fifth lines
            let selectedMessage = (evaluatedInputs[i] ? MESSAGES.get("invalidInput") : MESSAGES.get(gameStatusesList[i]));
            for (j = 0; j <= 1; j++) {
                let message = selectedMessage[j];
                let outerBoarder;
                if (evaluatedInputs[i] && j == 1) { // APPEND INVALID INPUT IN MESSAGE
                    if (args[i-1].length > 3) {
                        message += args[i-1].substring(0, 3) + "...";
                    }
                    else {
                        message += args[i-1];
                    }
                    if (actualboardLengths - selectedMessage[0].length < 0)
                        outerBoarder = selectedMessage[0].length - message.length;
                    else
                        outerBoarder = actualboardLengths - message.length;
                }else {
                    outerBoarder = actualboardLengths - message.length;
                }
                outerBoarder = (outerBoarder < 0 ? 0 : outerBoarder);
                let leftBound, rightBound;
                leftBound = rightBound = parseInt(outerBoarder / 2);
                if (outerBoarder % 2 != 0)
                    leftBound += 1;
                messageBox[j] = "│";
                for (k = 0; k < leftBound; k++) {
                    messageBox[j] += " ";
                }
                messageBox[j] += message;
                for (k = 0; k < rightBound; k++) {
                    messageBox[j] += " ";
                }
                messageBox[j] += "│";
            }
            // calculating the size of left margin
            let leftMarginLength = parseInt((messageBox[0].length - actualboardLengths) / 2);
            for (j = 0; j < leftMarginLength; j++) {
                leftMargin += " ";
            }
        }

        ////////// DRAWING TOP BOARDER
        let line = "╔════════╤";
        for (j = 0; j < boardPositionList[i][BOARD_INDEX]; j++) {
            line += "═══╤";
        }
        line += "═══════╗";
        currentBoard[boardCounter++] = leftMargin + line + leftMargin;
        ////////// DRAWING FOUR MIDDLE LINES
        if (evaluatedInputs[i] || gameStatusesList[i] != "gameContinue") {
            line = "┌";
            for (j = 0; j < messageBox[0].length - 2; j++) {
                line += "─";
            }
            line += "┐";
            currentBoard[boardCounter++] = line;
            currentBoard[boardCounter++] = messageBox[0];
            currentBoard[boardCounter++] = messageBox[1];
            line = "└";
            for (j = 0; j < messageBox[0].length - 2; j++) {
                line += "─";
            }
            line += "┘";
            currentBoard[boardCounter++] = line;
        } else {
            for (j = 0; j < 4; j++) {
                let boat = "";
                switch (j) {
                    case 0:
                        boat = "┌──┐1";
                        break;
                    case 1:
                    case 2:
                        let fishIndex = (j == 1 ? 1 : 3);
                        boat = "│" + (boardPositionList[i][fishIndex-1] == BOAT_POSITION ? fishIndex+1 : " ");
                        boat += (boardPositionList[i][fishIndex] == BOAT_POSITION ? fishIndex+2 : " ") + "│ ";
                        break;
                    case 3:
                        boat = "└──┘6";
                        break;
                }
                line = "║  " + boat + " │";
                for (k = 0; k < boardPositionList[i][BOARD_INDEX]; k++) {
                    if (k == boardPositionList[i][BOARD_INDEX] - SHADED_CELL_POSITION && k != boardPositionList[i][j])
                        line += " ▓ │";
                    else if (k == boardPositionList[i][j])
                        line += " " + (j + 2) + " │";
                    else
                        line += "   │";
                }
                if (boardPositionList[i][j] == boardPositionList[i][BOARD_INDEX])
                    line += "   " + (j + 2) + "   ║";
                else
                    line += "       ║";
                currentBoard[boardCounter++] = line;
            }
        }
        ////////// DRAWING BOTTOM BOARDER
        line = "╚════════╧";
        for (j = 0; j < boardPositionList[i][BOARD_INDEX]; j++) {
            line += "═══╧";
        }
        line += "═══════╝";
        currentBoard[boardCounter++] = leftMargin + line + leftMargin;
        boards.push(currentBoard);
    }
    return boards;
}

var main = async function(args) {
    // simulating an unsafe access to value - required for testing
    global.ARGS = args;
    await new PHQuarantine(getInput)
                    .bind(inputEvaluation)
                    .bind(boardPositions)
                    .bind(gameStatus)
                    .bind(boardCreation)
                    .execute();

}

if (require.main === module) {
    var args = process.argv.slice(2);
    main(args);
}

// Export the main function so it can be called from the tests
module.exports.main = main
