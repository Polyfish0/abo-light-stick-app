package de.polyfish0.adolightstick.service.nearbyconectivity

class Group(
    private var name: String,
    // I do not know if it is that smart to imply from that this device is the host if the currentHost == null but for now I do it that way
    private var currentHost: String?,
    private var maxUserCount: Int
) {
    private val connectedUserMap = HashSet<User>()

    fun addUser(user: User) {
        if(currentHost != null) {
            throw SecurityException("This device is not the owner of this group")
        }

        // TODO: replace the generic exception usage with dedicated exception classes
        if(connectedUserMap.size >= maxUserCount) {
            throw Exception("Group is full")
        }

        connectedUserMap.add(user)
    }

    fun removeUser(user: User) {
        if(currentHost != null) {
            throw SecurityException("This device is not the owner of this group")
        }

        connectedUserMap.remove(user)
    }

    fun getUsers(): List<User> {
        return connectedUserMap.map { it.copy() }
    }

    fun getName(): String {
        return name
    }
}