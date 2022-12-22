package com.is.mtc.pack;

import java.util.ArrayList;
import java.util.Random;

import com.is.mtc.MineTradingCards;
import com.is.mtc.data_manager.CardStructure;
import com.is.mtc.data_manager.Databank;
import com.is.mtc.root.Logs;
import com.is.mtc.root.Rarity;
import com.is.mtc.util.Reference;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Pack item drop informations
 * Drops up to 10 cards
 * cural: See table
 * Standard: 7, 2, 1 (Rare have a chance to be ancient or legendary)
 * Edition: Same as standard, but only from one edition
 */

public class PackItemRarity extends PackItemBase {

	private static final int[] cCount = new int[]{7, 2, 1, 0, 0};
	private static final int[] uCount = new int[]{6, 3, 1, 0, 0};
	private static final int[] rCount = new int[]{5, 3, 2, 0, 0};
	private static final int[] aCount = new int[]{3, 3, 3, 1, 0};
	private static final int[] lCount = new int[]{0, 0, 0, 0, 1};
	private static final int[][] tCount = {cCount, uCount, rCount, aCount, lCount};

	private static final String _str = "item_pack_";

	private int rarity;

	public PackItemRarity(int r) {
		setUnlocalizedName(_str + Rarity.toString(r).toLowerCase());
		setRegistryName(_str + Rarity.toString(r).toLowerCase());
		//setTextureName(MineTradingCards.MODID + ":" + _str + Rarity.toString(r).toLowerCase());

		rarity = r;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ArrayList<String> created;

		if (world.isRemote)
			return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));

		created = new ArrayList<String>();
		createCards(Rarity.COMMON, tCount[rarity][Rarity.COMMON], created, world.rand);
		createCards(Rarity.UNCOMMON, tCount[rarity][Rarity.UNCOMMON], created, world.rand);
		createCards(Rarity.RARE, tCount[rarity][Rarity.RARE], created, world.rand);
		createCards(Rarity.ANCIENT, tCount[rarity][Rarity.ANCIENT], created, world.rand);
		createCards(Rarity.LEGENDARY, tCount[rarity][Rarity.LEGENDARY], created, world.rand);

		if (created.size() > 0) {
			for (String cdwd : created) {
				spawnCard(player, world, cdwd);
			}
			player.getHeldItem(hand).setCount(player.getHeldItem(hand).getCount() - 1);
		} else {
			Logs.chatMessage(player, "Zero cards were registered, thus zero cards were generated");
			Logs.errLog("Zero cards were registered, thus zero cards can be generated");
		}

		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	@Override
	protected void createCards(int cardRarity, int count, ArrayList<String> created, Random random) {

		for (int x = 0; x < count; ++x) { // Generate x cards
			CardStructure cStruct = null;

			for (int y = 0; y < RETRY; ++y) { // Retry x times until...
				cStruct = Databank.generateACard(cardRarity, random);

				if (cStruct != null && !created.contains(cStruct.getCDWD())) { // ... cards was not already created. Duplicate prevention
					created.add(cStruct.getCDWD());
					break;
				}
			}
		}
	}
	
	
	// === ICON LAYERING AND COLORIZATION === //
	/** 
	 * From https://github.com/matshou/Generic-Mod
     */
	public static class ColorableIcon implements IItemColor 
	{
		private int rarity;
		
		public ColorableIcon(int r) {
			rarity = r;
		}
		
		@Override
	    @SideOnly(Side.CLIENT)
		public int colorMultiplier(ItemStack stack, int layer) 
		{
			if (layer==0)
	    	{
		    	switch (this.rarity)
		    	{
		    	case Rarity.COMMON:
		    		return MineTradingCards.PACK_COLOR_COMMON;
		    	case Rarity.UNCOMMON:
		    		return MineTradingCards.PACK_COLOR_UNCOMMON;
		    	case Rarity.RARE:
		    		return MineTradingCards.PACK_COLOR_RARE;
		    	case Rarity.ANCIENT:
		    		return MineTradingCards.PACK_COLOR_ANCIENT;
		    	case Rarity.LEGENDARY:
		    		return MineTradingCards.PACK_COLOR_LEGENDARY;
		    	}
		    	return Reference.COLOR_BLUE;
	    	}
	    	
	        return -1;
		}
	}

}