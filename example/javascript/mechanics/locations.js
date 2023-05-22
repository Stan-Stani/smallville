/**
 * A collection of predefined locations and their coordinates.
 * @typedef {Object.<string, {x: number, y: number}>} Locations
 */

/**
 * An object containing predefined locations and their coordinates.
 * @type {Locations}
 */
const locations = {
    "Red House": {
        x: -100,
        y: 100
    },
    "Green House": {
        x: -296,
        y: -140
    },
    "Campfire": {
        x: 590,
        y: 380
    },
    "Forest": {
        x: 490,
        y: 500
    },
    "Branches": {
        x: 810,
        y: 355
    },
    "Main Island": {
        x: 330,
        y: 110
    }
}

/**
 * Retrieves the coordinates of a location by name.
 * @param {string} name - The name of the location to retrieve coordinates for.
 * @returns {{x: number, y: number}} - The coordinates of the location.
 */
function getCoordinates(name) {
    const location = getLeafLocation(name)
    if (locations[location] == undefined){
        console.error(name + " does not exist")
        return
    }

    return { x: locations[location].x, y: locations[location].y }
}

function getLeafLocation(location) {
    const parts = location.split(':');
    return parts[parts.length - 1].trim().replace(":", "");
}

export { getCoordinates }


