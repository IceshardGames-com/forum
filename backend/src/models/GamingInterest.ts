import mongoose, { Schema, Document } from "mongoose";

export interface IGamingInterest extends Document {
  label: string; // Display name for UI
  value: string; // Slug or lowercase identifier
  category?: string; // Optional category key (e.g., "genre", "platform", etc.)
}

const GamingInterestSchema = new Schema<IGamingInterest>(
  {
    label: {
      type: String,
      required: true,
      trim: true,
    },
    value: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
    },
    category: {
      type: String,
      lowercase: true,
      trim: true,
      index: true,
    },
  },
  { timestamps: true,
    collection: "gaminginterests" 
  }
);

export default mongoose.model<IGamingInterest>(
  "GamingInterest",
  GamingInterestSchema
);
