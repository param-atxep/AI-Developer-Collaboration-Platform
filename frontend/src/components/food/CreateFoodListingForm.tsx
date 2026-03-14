import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Upload,
  X,
  MapPin,
  Calendar,
  Clock,
  Info,
} from 'lucide-react';
import toast from 'react-hot-toast';
import foodService from '@/services/foodService';
import type { FoodCategory, DietaryTag, Allergen, GeoLocation } from '@/types';
import LoadingSpinner from '@/components/common/LoadingSpinner';

const categories: { value: FoodCategory; label: string }[] = [
  { value: 'PREPARED_MEALS', label: 'Prepared Meals' },
  { value: 'FRESH_PRODUCE', label: 'Fresh Produce' },
  { value: 'BAKERY', label: 'Bakery' },
  { value: 'DAIRY', label: 'Dairy' },
  { value: 'CANNED_GOODS', label: 'Canned Goods' },
  { value: 'BEVERAGES', label: 'Beverages' },
  { value: 'SNACKS', label: 'Snacks' },
  { value: 'OTHER', label: 'Other' },
];

const dietaryOptions: { value: DietaryTag; label: string }[] = [
  { value: 'VEGAN', label: 'Vegan' },
  { value: 'VEGETARIAN', label: 'Vegetarian' },
  { value: 'HALAL', label: 'Halal' },
  { value: 'KOSHER', label: 'Kosher' },
  { value: 'GLUTEN_FREE', label: 'Gluten Free' },
  { value: 'NUT_FREE', label: 'Nut Free' },
  { value: 'DAIRY_FREE', label: 'Dairy Free' },
];

const allergenOptions: { value: Allergen; label: string }[] = [
  { value: 'PEANUTS', label: 'Peanuts' },
  { value: 'TREE_NUTS', label: 'Tree Nuts' },
  { value: 'MILK', label: 'Milk' },
  { value: 'EGGS', label: 'Eggs' },
  { value: 'WHEAT', label: 'Wheat' },
  { value: 'SOY', label: 'Soy' },
  { value: 'FISH', label: 'Fish' },
  { value: 'SHELLFISH', label: 'Shellfish' },
  { value: 'SESAME', label: 'Sesame' },
];

const units = ['servings', 'kg', 'lbs', 'boxes', 'bags', 'trays', 'pieces', 'liters'];

export default function CreateFoodListingForm() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState<FoodCategory>('PREPARED_MEALS');
  const [quantity, setQuantity] = useState('');
  const [unit, setUnit] = useState('servings');
  const [expiresAt, setExpiresAt] = useState('');
  const [pickupStart, setPickupStart] = useState('');
  const [pickupEnd, setPickupEnd] = useState('');
  const [pickupInstructions, setPickupInstructions] = useState('');
  const [dietaryTags, setDietaryTags] = useState<DietaryTag[]>([]);
  const [allergens, setAllergens] = useState<Allergen[]>([]);
  const [images, setImages] = useState<File[]>([]);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [location, setLocation] = useState<GeoLocation>({
    latitude: 0,
    longitude: 0,
    address: '',
  });

  const toggleDietaryTag = (tag: DietaryTag) => {
    setDietaryTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag],
    );
  };

  const toggleAllergen = (allergen: Allergen) => {
    setAllergens((prev) =>
      prev.includes(allergen) ? prev.filter((a) => a !== allergen) : [...prev, allergen],
    );
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (images.length + files.length > 5) {
      toast.error('Maximum 5 images allowed');
      return;
    }

    const newImages = [...images, ...files];
    setImages(newImages);

    files.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreviews((prev) => [...prev, reader.result as string]);
      };
      reader.readAsDataURL(file);
    });
  };

  const removeImage = (index: number) => {
    setImages((prev) => prev.filter((_, i) => i !== index));
    setImagePreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title || !quantity || !expiresAt || !pickupStart || !pickupEnd) {
      toast.error('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      await foodService.createListing({
        title,
        description,
        category,
        quantity: Number(quantity),
        unit,
        images,
        expiresAt,
        pickupStart,
        pickupEnd,
        pickupInstructions: pickupInstructions || undefined,
        allergens,
        dietaryTags,
        location: location.address ? location : undefined,
      });
      toast.success('Food listing created successfully!');
      navigate('/food');
    } catch {
      toast.error('Failed to create listing. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-8">
      {/* Basic Info */}
      <section>
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Basic Information
        </h2>
        <div className="grid gap-5">
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              Title <span className="text-red-500">*</span>
            </label>
            <input
              id="title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Fresh Vegetable Surplus from Lunch Service"
              className="input-field"
              required
            />
          </div>

          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              Description
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Describe the food items, condition, and any special notes..."
              className="input-field min-h-[100px] resize-y"
              rows={4}
            />
          </div>

          <div className="grid sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="category" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                Category <span className="text-red-500">*</span>
              </label>
              <select
                id="category"
                value={category}
                onChange={(e) => setCategory(e.target.value as FoodCategory)}
                className="input-field"
              >
                {categories.map((c) => (
                  <option key={c.value} value={c.value}>
                    {c.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label htmlFor="quantity" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                  Quantity <span className="text-red-500">*</span>
                </label>
                <input
                  id="quantity"
                  type="number"
                  min="1"
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                  placeholder="10"
                  className="input-field"
                  required
                />
              </div>
              <div>
                <label htmlFor="unit" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                  Unit
                </label>
                <select
                  id="unit"
                  value={unit}
                  onChange={(e) => setUnit(e.target.value)}
                  className="input-field"
                >
                  {units.map((u) => (
                    <option key={u} value={u}>
                      {u}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Images */}
      <section>
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Images
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-4">
          {imagePreviews.map((preview, index) => (
            <div key={index} className="relative group aspect-square rounded-xl overflow-hidden bg-gray-100 dark:bg-gray-800">
              <img src={preview} alt={`Preview ${index + 1}`} className="h-full w-full object-cover" />
              <button
                type="button"
                onClick={() => removeImage(index)}
                className="absolute top-2 right-2 p-1 rounded-full bg-black/50 text-white opacity-0 group-hover:opacity-100 transition-opacity"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
          ))}
          {images.length < 5 && (
            <label className="aspect-square rounded-xl border-2 border-dashed border-gray-300 dark:border-gray-600 flex flex-col items-center justify-center cursor-pointer hover:border-primary-400 dark:hover:border-primary-500 transition-colors">
              <Upload className="h-6 w-6 text-gray-400 mb-1" />
              <span className="text-xs text-gray-500 dark:text-gray-400">Upload</span>
              <input
                type="file"
                accept="image/*"
                multiple
                onChange={handleImageUpload}
                className="hidden"
              />
            </label>
          )}
        </div>
        <p className="mt-2 text-xs text-gray-400">Up to 5 images. JPG, PNG, WebP accepted.</p>
      </section>

      {/* Timing */}
      <section>
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Timing & Pickup
        </h2>
        <div className="grid gap-4">
          <div>
            <label htmlFor="expiresAt" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              <Calendar className="inline h-4 w-4 mr-1" />
              Expiry Date & Time <span className="text-red-500">*</span>
            </label>
            <input
              id="expiresAt"
              type="datetime-local"
              value={expiresAt}
              onChange={(e) => setExpiresAt(e.target.value)}
              className="input-field"
              required
            />
          </div>

          <div className="grid sm:grid-cols-2 gap-4">
            <div>
              <label htmlFor="pickupStart" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                <Clock className="inline h-4 w-4 mr-1" />
                Pickup Start <span className="text-red-500">*</span>
              </label>
              <input
                id="pickupStart"
                type="datetime-local"
                value={pickupStart}
                onChange={(e) => setPickupStart(e.target.value)}
                className="input-field"
                required
              />
            </div>
            <div>
              <label htmlFor="pickupEnd" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
                <Clock className="inline h-4 w-4 mr-1" />
                Pickup End <span className="text-red-500">*</span>
              </label>
              <input
                id="pickupEnd"
                type="datetime-local"
                value={pickupEnd}
                onChange={(e) => setPickupEnd(e.target.value)}
                className="input-field"
                required
              />
            </div>
          </div>

          <div>
            <label htmlFor="pickupInstructions" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
              <Info className="inline h-4 w-4 mr-1" />
              Pickup Instructions
            </label>
            <textarea
              id="pickupInstructions"
              value={pickupInstructions}
              onChange={(e) => setPickupInstructions(e.target.value)}
              placeholder="e.g. Come to the back entrance, ask for the manager..."
              className="input-field resize-y"
              rows={3}
            />
          </div>
        </div>
      </section>

      {/* Dietary & Allergens */}
      <section>
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Dietary Information
        </h2>

        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
            Dietary Tags
          </label>
          <div className="flex flex-wrap gap-2">
            {dietaryOptions.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => toggleDietaryTag(option.value)}
                className={`px-3 py-1.5 rounded-lg text-sm font-medium border transition-colors ${
                  dietaryTags.includes(option.value)
                    ? 'bg-primary-50 border-primary-500 text-primary-700 dark:bg-primary-900/30 dark:border-primary-500 dark:text-primary-400'
                    : 'border-gray-300 text-gray-600 hover:border-gray-400 dark:border-gray-600 dark:text-gray-400 dark:hover:border-gray-500'
                }`}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
            Allergens Present
          </label>
          <div className="flex flex-wrap gap-2">
            {allergenOptions.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => toggleAllergen(option.value)}
                className={`px-3 py-1.5 rounded-lg text-sm font-medium border transition-colors ${
                  allergens.includes(option.value)
                    ? 'bg-red-50 border-red-400 text-red-700 dark:bg-red-900/30 dark:border-red-500 dark:text-red-400'
                    : 'border-gray-300 text-gray-600 hover:border-gray-400 dark:border-gray-600 dark:text-gray-400 dark:hover:border-gray-500'
                }`}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* Location */}
      <section>
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Pickup Location
        </h2>
        <div>
          <label htmlFor="location-address" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1.5">
            <MapPin className="inline h-4 w-4 mr-1" />
            Address
          </label>
          <input
            id="location-address"
            type="text"
            value={location.address}
            onChange={(e) => setLocation({ ...location, address: e.target.value })}
            placeholder="Enter pickup address"
            className="input-field"
          />
        </div>
        <div className="mt-4 h-48 rounded-xl bg-gray-100 dark:bg-gray-800 border-2 border-dashed border-gray-300 dark:border-gray-600 flex items-center justify-center">
          <div className="text-center">
            <MapPin className="h-8 w-8 text-gray-400 mx-auto mb-2" />
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Click to pin location on map
            </p>
          </div>
        </div>
      </section>

      {/* Submit */}
      <div className="flex items-center justify-end gap-3 pt-6 border-t border-gray-200 dark:border-gray-800">
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="btn-outline"
        >
          Cancel
        </button>
        <button type="submit" disabled={loading} className="btn-primary gap-2">
          {loading ? <LoadingSpinner size="sm" /> : 'Publish Listing'}
        </button>
      </div>
    </form>
  );
}
