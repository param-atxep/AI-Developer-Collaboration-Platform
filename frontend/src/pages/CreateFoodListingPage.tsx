import CreateFoodListingForm from '@/components/food/CreateFoodListingForm';
import Sidebar from '@/components/common/Sidebar';

export default function CreateFoodListingPage() {
  return (
    <div className="flex min-h-[calc(100vh-4rem)]">
      <Sidebar />
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="mb-8">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              Create Food Listing
            </h1>
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Share your surplus food with the community. Fill in the details below.
            </p>
          </div>
          <div className="card p-6 sm:p-8">
            <CreateFoodListingForm />
          </div>
        </div>
      </div>
    </div>
  );
}
