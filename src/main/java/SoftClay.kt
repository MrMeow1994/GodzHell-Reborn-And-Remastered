object SoftClay {
    const val SOFT_CLAY: Int = 1761
    const val CLAY: Int = 434

    @JvmStatic
    fun makeClay(player: client) {
        if (!player.playerHasItem(CLAY)) {
            player.sendMessage("You need clay to do this.")
            return
        }
        player.isSpinning = true
        player.doAmount = player.getItemAmount(CLAY)
        EventManager.getSingleton().addEvent(player, object : Event {
            override fun execute(container: EventContainer) {
                if (player.playerHasItem(CLAY) && player.isSpinning) {
                    player.startAnimation(896)
                    player.deleteItem(CLAY, 1)
                    player.addItem(SOFT_CLAY, 1)
                    player.doAmount--
                    //  RandomEventHandler.addRandom(player);
                    player.sendMessage("You turn the clay into soft clay.")
                    if (player.disconnected || player.isSpinning == false || player.doAmount == 0) {
                        container.stop()
                        return
                    }
                }
            }

            override fun stop() {
                player.isSpinning = false
                player.startAnimation(65535)
                return
            }
        }, 3 * 600)
    }
}