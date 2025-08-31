package mod.journeycreative.blocks;

import mod.journeycreative.Journeycreative;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class EnderArchiveEntityRenderer implements BlockEntityRenderer<EnderArchiveBlockEntity> {
    private static final Identifier TEXTURE = Identifier.of(Journeycreative.MOD_ID, "textures/block/ender_archive.png");

    public EnderArchiveEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this(ctx.getLoadedEntityModels());
    }

    public EnderArchiveEntityRenderer(LoadedEntityModels models) {

    }

    public void render(EnderArchiveBlockEntity enderArchiveBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, Vec3d vec3d) {
        Direction direction = (Direction) enderArchiveBlockEntity.getCachedState().get(EnderArchiveBlock.FACING, Direction.NORTH);

        float[] g = enderArchiveBlockEntity.getBookTransparency(f);
        this.render(matrixStack, vertexConsumerProvider, i, j, direction, g);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Direction facing, float[] transparency) {
        float rotationDegrees = switch (facing) {
            case NORTH -> 0F;
            case SOUTH -> 180F;
            case WEST -> 90F;
            case EAST -> -90F;
            default -> 0F;
        };

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(new Quaternionf().rotateY((float)Math.toRadians(rotationDegrees)));
        matrices.multiply(new Quaternionf().rotateZ((float)Math.toRadians(180)));
        matrices.translate(-0.5, -0.5, -0.5);
        VertexConsumer bookTexture = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        Vec3i normalVec = facing.getVector();
        for (int i = 0; i < 6; i++) {
            renderbook(matrices.peek(), bookTexture, renderPos(i), uvRanges(i), transparency[i], light, normalVec);
        }
        matrices.pop();

        VertexConsumer endPortal = vertexConsumers.getBuffer(RenderLayer.getEndPortal());
        Matrix4f model = matrices.peek().getPositionMatrix();
        endPortal.vertex(model, No16(1), No16(15.5f), No16(15));
        endPortal.vertex(model, No16(15), No16(15.5f), No16(15));
        endPortal.vertex(model, No16(15), No16(15.5f), No16(1));
        endPortal.vertex(model, No16(1), No16(15.5f), No16(1));


    }

    private void renderbook(MatrixStack.Entry entry, VertexConsumer consumer, float[] renderPos, float[] uvRanges, float transparency, int light, Vec3i normal) {
        Matrix4f model = entry.getPositionMatrix();
        consumer.vertex(model, renderPos[0], renderPos[1], -0.01F)
                .color(1, 1, 1, transparency)
                .texture(uvRanges[0], uvRanges[1])
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, normal.getX(), normal.getY(), normal.getZ());
        consumer.vertex(model, renderPos[2], renderPos[1], -0.01F)
                .color(1, 1, 1, transparency)
                .texture(uvRanges[2], uvRanges[1])
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, normal.getX(), normal.getY(), normal.getZ());
        consumer.vertex(model, renderPos[2], renderPos[3], -0.01F)
                .color(1, 1, 1, transparency)
                .texture(uvRanges[2], uvRanges[3])
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, normal.getX(), normal.getY(), normal.getZ());
        consumer.vertex(model, renderPos[0], renderPos[3], -0.01F)
                .color(1, 1, 1, transparency)
                .texture(uvRanges[0], uvRanges[3])
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, normal.getX(), normal.getY(), normal.getZ());
    }

    private float[] renderPos(int bookNumber) {
        switch (bookNumber) {
            case 0: // 3, 2 - 5, 6
                return new float[]{No16(3), No16(2), No16(6), No16(7)};
            case 1: // 7, 2 - 9, 6
                return new float[]{No16(7), No16(2), No16(10), No16(7)};
            case 2: // 10, 3 - 12, 6
                return new float[]{No16(10), No16(3), No16(13), No16(7)};
            case 3: // 3, 9 - 6, 13
                return new float[]{No16(3), No16(9), No16(7), No16(14)};
            case 4: // 7, 10 - 9, 13
                return new float[]{No16(7), No16(9), No16(10), No16(14)};
            case 5: // 11, 9 - 13, 13
                return new float[]{No16(11), No16(9), No16(14), No16(14)};
            default:
                return new float[]{No16(3), No16(2), No16(6), No16(7)};
        }
    }

    private float[] uvRanges(int bookNumber) {
        switch (bookNumber){
            case 0: // 48, 16 - 50, 20
                return new float[]{48.0F / 64, 16.0F / 64, 51.0F / 64, 21.0F / 64};
            case 1: // 52, 16 - 54, 20
                return new float[]{52.0F / 64, 16.0F / 64, 55.0F / 64, 21.0F / 64};
            case 2: // 55, 17 - 57, 20
                return new float[]{55.0F / 64, 17.0F / 64, 58.0F / 64, 21.0F / 64};
            case 3: // 48, 23 - 51, 27
                return new float[]{48.0F / 64, 23.0F / 64, 52.0F / 64, 28.0F / 64};
            case 4: // 52, 24 - 54, 27
                return new float[]{52.0F / 64, 23.0F / 64, 55.0F / 64, 28.0F / 64};
            case 5: // 56, 23 - 58, 27
                return new float[]{56.0F / 64, 23.0F / 64, 59.0F / 64, 28.0F / 64};
            default:
                return new float[]{48.0F / 64, 16.0F / 64, 51.0F / 64, 21.0F / 64};
        }
    }

    private float No16(float f) {
        return f / 16.0F;
    }
}
