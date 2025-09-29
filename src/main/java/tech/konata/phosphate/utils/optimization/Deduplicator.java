package tech.konata.phosphate.utils.optimization;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.model.IBakedModel;
import tech.konata.phosphate.utils.logging.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Deduplicator {
    private static final Map<String, String> VARIANT_IDENTITIES = new ConcurrentHashMap<>();
    // Typedefs would be a nice thing to have
    private static final Map<List<Predicate<IBlockState>>, Predicate<IBlockState>> OR_PREDICATE_CACHE = new ConcurrentHashMap<>();
    private static final Map<List<Predicate<IBlockState>>, Predicate<IBlockState>> AND_PREDICATE_CACHE = new ConcurrentHashMap<>();
    private static final ObjectOpenCustomHashSet<int[]> BAKED_QUAD_CACHE = new ObjectOpenCustomHashSet<>(
            new LambdaBasedHash<>(Deduplicator::betterIntArrayHash, Arrays::equals)
    );

    private static final Logger LOGGER = new Logger("Deduplicator");

    public static String deduplicateVariant(String variant) {
        return VARIANT_IDENTITIES.computeIfAbsent(variant, Function.identity());
    }

    /**
     * An alternative to Arrays::hashCode for int arrays that appears to be more collision resistant for baked quad
     * vertex data arrays. Arrays::hashCode seems to be prone to collisions when arrays only differ slightly; this
     * caused the slowdown observed in FerriteCore issue #129.
     */
    private static int betterIntArrayHash(int[] in) {
        int result = 0;
        for (int i : in) {
            result = 31 * result + HashCommon.murmurHash3(i);
        }
        return result;
    }

    public static void deduplicate(BakedQuad bq) {
        synchronized (BAKED_QUAD_CACHE) {
            bq.vertexData = BAKED_QUAD_CACHE.addOrGet(bq.getVertexData());
        }
    }

    public static void registerReloadListener() {
        // Register the reload listener s.t. its "sync" part runs after the model loader reload
        ((SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new IResourceManagerReloadListener() {
            @Override
            public void onResourceManagerReload(IResourceManager resourceManager) {
                VARIANT_IDENTITIES.clear();
//                KNOWN_MULTIPART_MODELS.clear();
                OR_PREDICATE_CACHE.clear();
                AND_PREDICATE_CACHE.clear();
                synchronized (BAKED_QUAD_CACHE) {
                    BAKED_QUAD_CACHE.clear();
                    BAKED_QUAD_CACHE.trim();
                }
            }
        });
    }
}