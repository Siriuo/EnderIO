package crazypants.enderio.machine.solar;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.EnergyStorage;
import cpw.mods.fml.common.registry.GameRegistry;
import crazypants.enderio.BlockEio;
import crazypants.enderio.ModObject;
import crazypants.enderio.api.tool.ITool;
import crazypants.enderio.config.Config;
import crazypants.enderio.gui.IResourceTooltipProvider;
import crazypants.enderio.tool.ToolUtil;
import crazypants.enderio.waila.IWailaInfoProvider;
import crazypants.enderio.waila.WailaCompat;
import crazypants.util.Lang;
import crazypants.util.Util;

public class BlockSolarPanel extends BlockEio implements IResourceTooltipProvider, IWailaInfoProvider {

  public static int renderId;

  public static BlockSolarPanel create() {
    BlockSolarPanel result = new BlockSolarPanel();
    result.init();
    return result;
  }

  private static final float BLOCK_HEIGHT = 0.15f;

  IIcon sideIcon;
  IIcon advancedSideIcon;
  IIcon advancedIcon;

  IIcon borderIcon;
  IIcon advancedBorderIcon;

  private BlockSolarPanel() {
    super(ModObject.blockSolarPanel.unlocalisedName, TileEntitySolarPanel.class);
    if(!Config.photovoltaicCellEnabled) {
      setCreativeTab(null);
    }
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, BLOCK_HEIGHT, 1.0F);
  }

  @Override
  protected void init() {
    GameRegistry.registerBlock(this, BlockItemSolarPanel.class, name);
    if(teClass != null) {
      GameRegistry.registerTileEntity(teClass, name + "TileEntity");
    }
  }

  @Override
  public int damageDropped(int damage) {
    return damage;
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9) {
    ITool tool = ToolUtil.getEquippedTool(entityPlayer);
    if(tool != null && entityPlayer.isSneaking() && tool.canUse(entityPlayer.getCurrentEquippedItem(), entityPlayer, x, y, z)) {
      if(!world.isRemote && !entityPlayer.capabilities.isCreativeMode) {
        int meta = world.getBlockMetadata(x, y, z);
        ItemStack is = new ItemStack(this, 1, meta);
        Util.dropItems(world, is, x, y, z, true);
      }
      removedByPlayer(world, entityPlayer, x, y, z, true);
      tool.used(entityPlayer.getCurrentEquippedItem(), entityPlayer, x, y, z);
      return true;
    }
    return false;
  }

  @Override
  public IIcon getIcon(int side, int meta) {
    if(side == ForgeDirection.UP.ordinal()) {
      return meta == 0 ? blockIcon : advancedIcon;
    }
    return meta == 0 ? sideIcon : advancedSideIcon;
  }

  public IIcon getBorderIcon(int i, int meta) {
    return meta == 0 ? borderIcon : advancedBorderIcon;
  }

  @Override
  public int getRenderType() {
    return renderId;
  }

  @Override
  public void onNeighborBlockChange(World world, int x, int y, int z, Block par5) {
    TileEntity te = world.getTileEntity(x, y, z);
    if(te instanceof TileEntitySolarPanel) {
      ((TileEntitySolarPanel) te).onNeighborBlockChange();
    }
  }

  @Override
  public void registerBlockIcons(IIconRegister register) {
    blockIcon = register.registerIcon("enderio:solarPanelTop");
    advancedIcon = register.registerIcon("enderio:solarPanelAdvancedTop");
    sideIcon = register.registerIcon("enderio:solarPanelSide");
    advancedSideIcon = register.registerIcon("enderio:solarPanelAdvancedSide");
    borderIcon = register.registerIcon("enderio:solarPanelBorder");
    advancedBorderIcon = register.registerIcon("enderio:solarPanelAdvancedBorder");
  }

  @Override
  public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, BLOCK_HEIGHT, 1.0F);
  }

  @Override
  public void setBlockBoundsForItemRender() {
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, BLOCK_HEIGHT, 1.0F);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void addCollisionBoxesToList(World par1World, int par2, int par3, int par4, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity par7Entity) {
    setBlockBoundsBasedOnState(par1World, par2, par3, par4);
    super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
  }

  @Override
  public String getUnlocalizedNameForTooltip(ItemStack itemStack) {
    return getUnlocalizedName();
  }

  @Override
  public void getWailaInfo(List<String> tooltip, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(x, y, z);
    if(te instanceof TileEntitySolarPanel) {
      TileEntitySolarPanel solar = (TileEntitySolarPanel) te;
      float efficiency = solar.calculateLightRatio();
      EnergyStorage storage = new EnergyStorage(Integer.MAX_VALUE);
      storage.readFromNBT(WailaCompat.getNBTData());
      tooltip.add(storage.getEnergyStored() + " / " + WailaCompat.getNBTData().getInteger("rfCap") + " RF");
      tooltip.add(String.format("%s : %s%.0f%%", EnumChatFormatting.WHITE + Lang.localize("tooltip.efficiency") + EnumChatFormatting.RESET,
          EnumChatFormatting.WHITE, efficiency * 100));
    }
  }

  @Override
  public int getDefaultDisplayMask(World world, int x, int y, int z) {
    return 0;
  }
}
