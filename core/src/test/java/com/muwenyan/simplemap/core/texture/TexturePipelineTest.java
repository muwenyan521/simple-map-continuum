package com.muwenyan.simplemap.core.texture;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TexturePipelineTest {
    @Test
    void atlasPackingIsSortedAndBounded() {
        TextureTile a = new TextureTile("b", 2, 1, new byte[8]);
        TextureTile b = new TextureTile("a", 3, 2, new byte[24]);
        TextureAtlasLayout layout = TextureAtlasPacker.pack(List.of(a, b), 4);
        assertEquals(List.of("a", "b"), layout.regions().stream().map(TextureRegion::id).toList());
        assertEquals(3, layout.width());
        assertEquals(3, layout.height());
        assertThrows(IllegalArgumentException.class, () -> TextureAtlasPacker.pack(List.of(a, a), 4));
    }

    @Test
    void uploadBudgetPreservesQueueOrder() {
        UploadBudget budget = new UploadBudget();
        budget.enqueue(new UploadBudget.UploadRequest("a", 10, 1));
        budget.enqueue(new UploadBudget.UploadRequest("b", 20, 1));
        assertEquals(List.of("a"), budget.drain(15).stream().map(UploadBudget.UploadRequest::id).toList());
        assertEquals(1, budget.pendingCount());
    }
}
