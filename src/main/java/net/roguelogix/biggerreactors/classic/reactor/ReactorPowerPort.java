package net.roguelogix.biggerreactors.classic.reactor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.roguelogix.phosphophyllite.registry.RegisterBlock;

import javax.annotation.Nullable;

import static net.roguelogix.biggerreactors.classic.reactor.ReactorPowerPort.ConnectionState.CONNECTION_STATE_ENUM_PROPERTY;

@RegisterBlock(name = "reactor_power_port", tileEntityClass = ReactorPowerPortTile.class)
public class ReactorPowerPort extends ReactorBaseBlock{

    @RegisterBlock.Instance
    public static ReactorPowerPort INSTANCE;

    public ReactorPowerPort() {
        super();
        setDefaultState(getDefaultState().with(CONNECTION_STATE_ENUM_PROPERTY, ConnectionState.DISCONNECTED));
    }

    enum ConnectionState implements IStringSerializable {
        CONNECTED,
        DISCONNECTED;

        public static final EnumProperty<ConnectionState> CONNECTION_STATE_ENUM_PROPERTY = EnumProperty.create("connectionstate", ConnectionState.class);
        @Override
        public String getName() {
            return toString().toLowerCase();
        }

    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(CONNECTION_STATE_ENUM_PROPERTY);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ReactorPowerPortTile();
    }
}
