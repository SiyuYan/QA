class AddFuzzLevelToTests < ActiveRecord::Migration
  def change
    add_column :tests, :fuzz_level, :string
  end
end
